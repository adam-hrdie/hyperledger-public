package hm.node;

import static hm.constants.HyperledgerConstants.*;
import static hm.node.ConnectionProperties.*;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.*;
import java.util.concurrent.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.slf4j.*;

import com.google.gson.*;

import hm.api.BlockReport;
import hm.api.LastBlockProcessed;
import hm.api.MessageType;
import hm.api.StartingBlock;
import hm.beans.*;
import hm.channel.*;
import hm.node.auth.*;
import hm.util.EncodedBase64Deserialiser;

public class HyperLedgerNodeImpl implements HyperLedgerNode {
   private static final Logger LOG = LoggerFactory.getLogger(HyperLedgerNodeImpl.class);

   private final String name;
   private static final Gson customGson = new GsonBuilder().registerTypeHierarchyAdapter(byte[].class, new EncodedBase64Deserialiser()).create();
   private static final Gson GSON = new Gson();

   private HFClient client;
   private Channel channel;
   private final CertificateManager certificateManager;
   private final ChannelPlayer channelPlayer;
   @SuppressWarnings("FieldCanBeLocal")
   private final long MESSAGE_TIMEOUT_SECS = 5;

   public HyperLedgerNodeImpl(String name) throws Exception {
      certificateManager = new CertificateManagerImpl();
      channelPlayer = new ChannelPlayerImpl();
      this.name = name;
   }

   public HyperLedgerNodeImpl(final CertificateManager certificateManager, final ChannelPlayer channelPlayer, String name) {
      this.certificateManager = certificateManager;
      this.channelPlayer = channelPlayer;
      this.name = name;
   }

   /**
    * Register and enroll a client with userId. If user is already serialized
    * on fs it will be loaded and registration and enrollment will be skipped.
    *
    * Once initialised, the class connects to the peer, orderer and default
    * channel.
    */
   @Override
   public void startWithDefaultUrl(String username) throws Exception {
      // use CA to enroll a client if we do not have one serialised in file system
      client = certificateManager.initialize(username);
      doConnect();
   }

   private void doConnect() throws InvalidArgumentException {
      channel = createChannel();
   }

   private Channel createChannel() throws InvalidArgumentException {
      // Initialise channel
      // peer name and endpoint in network
      final Peer peer = client.newPeer(ConnectionProperties.PEER0_NAME, ConnectionProperties.HL_PEER0);

      // orderer name and endpoint in network
      final Orderer orderer = client.newOrderer(ConnectionProperties.ORDERER_NAME, ConnectionProperties.HL_ORDERER, getOrdererProperties());

      // channel name in network
      try {
         final Channel channel = client.newChannel(ConnectionProperties.CHANNEL);
         channel.addOrderer(orderer);
         channel.addPeer(peer);
         channel.initialize();
         return channel;
      }
      catch (final Exception e) {
         LOG.error("failed to create channel", e);
      }
      return null;
   }

   /**
    * clone an initialised channel and catch up events from the required block
    * Assumes the class has had startWithDefaultUrl
    */
   @Override
   public Map<Long, List<BlockchainEventWrapper>> catchUpEvents(final ChaincodeEventListener chaincodeEventListener, final String eventName, final Map<Long, List<BlockchainEventWrapper>> orderedEvents, final StartingBlock startingBlock) {
      if (isConnected() == false) {
         LOG.error("catchUpEvents : -failed- node is not connected to a channel!");
         return orderedEvents;
      }

      final long fromBlock;
      final long toBlock = -1L;
      final LastBlockProcessed lbp;

      try {
         final long blockchainHeight = channel.queryBlockchainInfo().getHeight();

         if (startingBlock == StartingBlock.LAST_PROCESSED) {
            lbp = this.getLastBlockProcessed();
            if (lbp == null) {
               LOG.warn("catchUpEvents : startingBlock = LAST_PROCESSED but lastBlockProcessed was not found on chain. Not replaying any blocks");
               return orderedEvents;
            }
            fromBlock = lbp.getBlockNumber() + 1;
         }

         else if (startingBlock == StartingBlock.GENESIS)
            fromBlock = 0;
         else
            return orderedEvents;

         LOG.debug("catchUpEvents : channel[{}], is about to be read from block [{}]. Total blockchain height [{}]", ConnectionProperties.CHANNEL, fromBlock, blockchainHeight);

         //create a clone of the channel, that is not initialised on the blockchain
         final byte[] replayChannelBytes = channel.serializeChannel();

         //ensure the original channel is not running at the same time
         channel.shutdown(true);

         final Channel replayTestChannel = client.deSerializeChannel(replayChannelBytes);
         channelPlayer.playChannelChaincodeEvents(client, replayTestChannel, chaincodeEventListener, fromBlock, toBlock, eventName, CATCHUP_BLOCKS_TIMEOUT);

         //swap the channel back to the original
         replayTestChannel.shutdown(true);

         LOG.debug("catchUpEvents : got a total of {} events to catch up from channel [{}]", orderedEvents.size(), ConnectionProperties.CHANNEL);

         channel = createChannel();
      }
      catch (final Exception e) {
         LOG.error("failed to catchUpEvents", e);
      }
      return orderedEvents;
   }

   /**
    * Register an event listener Assumes the class has had startWithDefaultUrl
    *
    * @param chaincodeEventListener
    *           The required listener for the channel
    */
   @Override
   public void subscribeToChannel(final ChaincodeEventListener chaincodeEventListener, final String eventName) throws InvalidArgumentException {
      LOG.debug("subscribeToChannel : attempting subscribe to channel[{}] using peer[{}], orderer[{}] & registering for chaincode events of type[{}]", ConnectionProperties.CHANNEL, ConnectionProperties.PEER0_NAME, ConnectionProperties.ORDERER_NAME, eventName);

      if (isConnected() == false) {
         LOG.error("subscribeToChannel : -failed- node is not connected to a channel!");
         return;
      }

      // set listener to the channel chaincode event we want to handle
      channel.registerChaincodeEventListener(Pattern.compile(".*"), Pattern.compile(Pattern.quote(eventName)), chaincodeEventListener);
      LOG.info("subscribeToChannel : -Success-");
   }

   /**
    * Serialise a MessageBean into JSON format, and try to commit it to the
    * ledger. Assumes the class has had startWithDefaultUrl called first
    * Currently atomic - perform all three parts of the commit as one -
    * including waiting for endorsement
    *
    * @param message
    *           the message to send
    */
   @Override
   public void commitMessage(final MessageBean message, final String chaincodeMethod) throws Exception {
	  LOG.info("[{}] is committing message [{}]", name, message.toString());
      commit(message, chaincodeMethod);
   }

   private void commit(final MessageBean message, final String chaincodeMethod) throws ProposalException, InvalidArgumentException, TransactionException {
      LOG.debug("commitMessage : channel[{}], chaincode method[{}], message:[{}]", ConnectionProperties.CHANNEL, chaincodeMethod, message);

      final Channel chnl = client.getChannel(ConnectionProperties.CHANNEL);

      final Collection<ProposalResponse> responses = proposeNewTransaction(message, chnl, chaincodeMethod);

      final List<ProposalResponse> invalid = responses.stream().filter(ProposalResponse::isInvalid).collect(Collectors.toList());

      if (invalid.isEmpty()) {
         LOG.debug("commitMessage : channel[{}] has successfully endorsed message id[{}]; attempting commit", ConnectionProperties.CHANNEL, message.getId());
         commitMessage(message, chnl, responses);
      }
      else {
         logInvalidProposalResponse(message, invalid);
         throw new ProposalException("commitMessage : channel[" + ConnectionProperties.CHANNEL + "] has rejected the transaction proposal for message id[" + message.getId() + "]");
      }
   }

   private void commitMessage(final MessageBean message, final Channel chnl, final Collection<ProposalResponse> responses) throws TransactionException {

      final boolean sentSuccessfully;

      try {
         sentSuccessfully = chnl.sendTransaction(responses).get(MESSAGE_TIMEOUT_SECS, SECONDS).isValid();
      }
      catch (final InterruptedException | ExecutionException | TimeoutException e) {
         throw new TransactionException("Failed to send Transaction to channel [" + ConnectionProperties.CHANNEL + "] for message id [" + message.getId() + "]", e);
      }

      if (sentSuccessfully)
         LOG.info("[{}] successfully committed message : {}", name, message);
      else {
         LOG.warn("commitMessage : error committing message: {}", message);
         throw new TransactionException("Failed to send TransactionEvent to channel [" + ConnectionProperties.CHANNEL + "] for message id [" + message.getId() + "]");
      }

   }

   /**
    * Serialise a MessageBean into JSON format, and try to commit it to the
    * ledger. Assumes the class has had startWithDefaultUrl called first
    * Currently atomic - perform all three parts of the commit as one -
    * including waiting for endorsement
    *
    * @param message
    *           the message to send
    * @return true if message was successfully sent, false otherwise
    */
   @Override
   public boolean commitMessageResponse(final MessageBean message) throws Exception {
	  LOG.info("[{}] is committing message [{}]", name, message.toString());
      commit(message, CHAINCODE_ACK_METHOD);
      return true;
   }

   private void logInvalidProposalResponse(final MessageBean message, final List<ProposalResponse> invalid) {
      invalid.forEach(response -> LOG.error("MessageBean: {}. Response: {}", message, response.getMessage()));
   }

   private Collection<ProposalResponse> proposeNewTransaction(final MessageBean message, final Channel channel, final String chainCodeMethod)
         throws ProposalException, InvalidArgumentException {

      LOG.debug("proposeNewTransaction : proposing transaction for peer validation chaincode[{}] method[{}], messageId[{}]", ConnectionProperties.CHAINCODE, chainCodeMethod, message
            .getId());

      final TransactionProposalRequest tpr = client.newTransactionProposalRequest();
      final ChaincodeID cid = ChaincodeID.newBuilder().setName(ConnectionProperties.CHAINCODE).build();
      tpr.setChaincodeID(cid);
      tpr.setFcn(chainCodeMethod);
      final String[] args = new String[] { GSON.toJson(message) };
      tpr.setArgs(args);
      return channel.sendTransactionProposal(tpr);
   }

   @Override
   public void commitLastBlockProcessed(final BlockReport blockReport) throws Exception {
      final byte[] object = GSON.toJson(new LastBlockProcessed(blockReport)).getBytes();
      final MessageBean message = new MessageBean(/*guid is not required as we will persist by component certificate*/"-LBP-", MessageType.LAST_BLOCK_PROCESSED.getValue(), object, System.currentTimeMillis());
      commitMessage(message, "init_message");
   }

   @Override
   public LastBlockProcessed getLastBlockProcessed() {
      Channel channel = this.channel;
      boolean throwChannel = false;
      MessageBean message = null;
      LastBlockProcessed lbp = null;

      try {
         if (this.isConnected() == false) {
            channel = createChannel();
            throwChannel = true;
         }

         final QueryByChaincodeRequest qpr = client.newQueryProposalRequest();
         final ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(CHAINCODE).build();
         qpr.setChaincodeID(chaincodeID);
         qpr.setFcn(CHAINCODE_LBP);
         final Collection<ProposalResponse> res = channel.queryByChaincode(qpr);

         for (final ProposalResponse pres : res) {

            final String stringResponse = new String(pres.getChaincodeActionResponsePayload());
            if (pres.isInvalid() || stringResponse.isEmpty())
               return null;

            message = customGson.fromJson(stringResponse, MessageBean.class);
            lbp = GSON.fromJson(new String(message.getValue()), LastBlockProcessed.class);
            LOG.info("LastBlockProcessed for user {} is : {} ", client.getUserContext().getName(), lbp.toString());
         }
      }
      catch (final JsonSyntaxException e) {
         LOG.error("Unable to deserialize json! MessageBean received was {} ", message, e);
      }
      catch (final Exception e) {
         LOG.error("getLastBlockProcessed: failed!", e);
      }
      catch (final AssertionError e) {
         LOG.error("Assertion Error... ", e);
      }

      finally {
         if (throwChannel == true)
            channel.shutdown(true);
      }
      return lbp;
   }

   @Override
   public long getBlockchainHeight() {
      try {
         if (isConnected())
            return channel.queryBlockchainInfo().getHeight();
         else
            LOG.error("getBlockchainHeight failed : node is not connected to any channel");
      }
      catch (final ProposalException | InvalidArgumentException e) {
         LOG.error("getBlockchainHeight: failed!", e);
      }
      return -1L;
   }

   @Override
   public HFClient getClient() {
      return client;
   }

   @Override
   public void setClient(final HFClient client) {
      this.client = client;
   }

   @Override
   public void shutdown() {
      if (isConnected())
         channel.shutdown(true);
   }

   @Override
   public boolean isConnected() {
      return channel != null && channel.isShutdown() == false;
   }
   
   @Override
   public String name() {
	   return name;
   }
}