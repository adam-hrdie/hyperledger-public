package hm.node;

import java.util.*;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;

import hm.api.BlockReport;
import hm.api.LastBlockProcessed;
import hm.api.StartingBlock;
import hm.beans.*;

public interface HyperLedgerNode {

   void startWithDefaultUrl() throws Exception;

   boolean commitMessageResponse(MessageBean message) throws Exception;

   void shutdown();

   void subscribeToChannel(ChaincodeEventListener chaincodeEventListener, String eventName) throws InvalidArgumentException;

   boolean isConnected();

   void commitLastBlockProcessed(BlockReport blockReport) throws Exception;

   LastBlockProcessed getLastBlockProcessed() throws InvalidArgumentException, TransactionException;

   long getBlockchainHeight();

   Map<Long, List<BlockchainEventWrapper>> catchUpEvents(ChaincodeEventListener chaincodeEventListener, String eventName, Map<Long, List<BlockchainEventWrapper>> orderedEvents, StartingBlock startingBlock);

   void setClient(HFClient client);

   HFClient getClient();

   void commitMessage(MessageBean message, String chaincodeMethod) throws Exception;

   String name();

}
