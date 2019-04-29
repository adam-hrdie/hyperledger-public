package hm.util;

import org.hyperledger.fabric.sdk.*;
import org.slf4j.*;

import com.google.gson.Gson;

import hm.api.BlockReport;
import hm.beans.*;

public class BlockchainEventDecoder {
   private static final Logger LOG = LoggerFactory.getLogger(BlockchainEventDecoder.class);

   private final static Gson GSON = new Gson();

   public static BlockchainEventWrapper decode(final BlockEvent blockEvent, final ChaincodeEvent chaincodeEvent) {
      String json = "";
      try {
         final String receivingFrom = blockEvent.getPeer() == null ? "eventHub" : blockEvent.getPeer().getName();
         json = new String(chaincodeEvent.getPayload());
         final MessageBean bean = GSON.fromJson(json, MessageBean.class);
         final BlockReport report = new BlockReport(blockEvent.getBlockNumber(), blockEvent.getDataHash());

         LOG.trace("decode : Found Event In Block #{} EventName: {} ReceivedFrom: {} TxId: {} Message: {}", blockEvent.getBlockNumber(), chaincodeEvent
               .getEventName(), receivingFrom, chaincodeEvent.getTxId(), bean);

         return new BlockchainEventWrapper(report, bean);
      }
      catch (final Exception e) {
         LOG.error("Failed to decode event. Json was [{}]", json, e);
         return null;
      }
   }

}
