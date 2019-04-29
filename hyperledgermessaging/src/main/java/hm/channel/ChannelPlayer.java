package hm.channel;

import org.hyperledger.fabric.sdk.*;

public interface ChannelPlayer {

   void playChannelChaincodeEvents(HFClient client, Channel replayTestChannel, ChaincodeEventListener chaincodeEventListener, long fromBlock, long toBlock, String eventName, int timeoutSeconds);

}
