package hm.listener;

import java.util.*;
import java.util.concurrent.BlockingQueue;

import org.hyperledger.fabric.sdk.*;
import org.slf4j.*;

import hm.beans.BlockchainEventWrapper;
import hm.util.BlockchainEventDecoder;

public class ChaincodeEventListenerImpl implements ChaincodeEventListener {
   private static final Logger LOG = LoggerFactory.getLogger(ChaincodeEventListenerImpl.class);

   private final BlockingQueue<BlockchainEventWrapper> queue;
   final Map<Long, List<BlockchainEventWrapper>> orderingMap;

   public ChaincodeEventListenerImpl(final BlockingQueue<BlockchainEventWrapper> queue) {
      this.queue = queue;
      this.orderingMap = null;
   }

   public ChaincodeEventListenerImpl(final Map<Long, List<BlockchainEventWrapper>> orderingMap) {
      this.queue = null;
      this.orderingMap = orderingMap;
   }

   @Override
   public void received(final String handle, final BlockEvent blockEvent, final ChaincodeEvent chaincodeEvent) {

      //noinspection deprecation
      if (blockEvent.getEventHub() != null) { //keeping this here for safety until removed
         LOG.warn("received : EventHub event received possible duplication of peer event. Ignoring TxId: {}", chaincodeEvent.getTxId());
         return;
      }

      processMessage(BlockchainEventDecoder.decode(blockEvent, chaincodeEvent));
   }

   private void processMessage(final BlockchainEventWrapper event) {
      if (event == null)
         return;

      final ArrayList<BlockchainEventWrapper> list = new ArrayList<>();
      list.add(event);

      try {

         if (queue != null) {
            LOG.info("adding to process queue: block #[{}] event {}", event.blockReport().number(), event.messageBean());
            queue.put(event);
         }

         else if (orderingMap != null) {
            LOG.info("read block [{}] into ordered map for processing ", event.blockReport().number());

            if (orderingMap.get(event.blockReport().number()) != null)
               list.addAll(orderingMap.get(event.blockReport().number()));

            orderingMap.put(event.blockReport().number(), list);
         }
      }
      catch (final InterruptedException e) {
         Thread.currentThread().interrupt();
         LOG.error("Interrupted while waiting:", e);
      }
      catch (final Exception e) {
         LOG.error("Caught exception while processing event {}:", event, e);
      }

   }
}