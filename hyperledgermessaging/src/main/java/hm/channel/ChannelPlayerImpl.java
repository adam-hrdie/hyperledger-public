package hm.channel;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.Channel.PeerOptions;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.slf4j.*;

public class ChannelPlayerImpl implements ChannelPlayer {
   private static final Logger LOG = LoggerFactory.getLogger(ChannelPlayer.class);

   @Override
   public void playChannelChaincodeEvents(final HFClient client, final Channel replayTestChannel, final ChaincodeEventListener chaincodeEventListener, final long fromBlock, final long toBlock, final String eventName, final int timeoutSeconds) {
      try {
         final List<Peer> savedPeers = new ArrayList<>(replayTestChannel.getPeers());
         for (final Peer peer : savedPeers) {
            replayTestChannel.removePeer(peer);
         }

         Peer eventingPeer = savedPeers.remove(0);
         eventingPeer = client.newPeer(eventingPeer.getName(), eventingPeer.getUrl(), eventingPeer.getProperties());

         final PeerOptions eventingPeerOptions = PeerOptions.createPeerOptions().setPeerRoles(EnumSet.of(PeerRole.EVENT_SOURCE));

         if (-1L == toBlock) { //replay to end
            replayTestChannel.addPeer(eventingPeer, eventingPeerOptions.startEvents(fromBlock));
         }
         else {
            replayTestChannel.addPeer(eventingPeer, eventingPeerOptions.startEvents(fromBlock).stopEvents(toBlock));
         }

         final String listenerHandle = replayTestChannel.registerChaincodeEventListener(Pattern.compile(".*"), Pattern.compile(Pattern.quote(eventName)), chaincodeEventListener);

         try {
            LOG.debug("processing channel blocks...", timeoutSeconds);
            replayTestChannel.initialize();
            TimeUnit.SECONDS.sleep(timeoutSeconds);
            replayTestChannel.unregisterChaincodeEventListener(listenerHandle);
            LOG.debug("done!", timeoutSeconds);
         }
         catch (final Exception e) {
            LOG.error("playChannelChaincodeEvents : failed!", e);
         }
      }
      catch (final Exception e) {
         LOG.error("playChannelChaincodeEvents : failed!", e);
      }
   }

}
