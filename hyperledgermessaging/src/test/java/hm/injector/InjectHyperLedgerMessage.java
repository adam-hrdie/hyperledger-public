package hm.injector;

import java.util.UUID;

import org.slf4j.*;

import hm.beans.MessageBean;
import hm.node.HyperLedgerNode;

public class InjectHyperLedgerMessage extends HyperLedgerAbstractInjector {

   private static final Logger LOG = LoggerFactory.getLogger(InjectHyperLedgerMessage.class);
   public static final String randomJson = "[false,false,true,\"gently\",[{\"per\":\"detail\",\"hurried\":true,\"instead\":1630003982.4614763,\"only\":\"happily\",\"experience\":false,\"coal\":\"success\"},false,\"mice\",-909580484,-1320479408,\"freedom\"],\"electricity\"]";

   public InjectHyperLedgerMessage() {

   }

   public void inject(final String chaincodeMethodName) {
      try {
         LOG.info("Starting");

         init();
         inject(createMessageBean(), chaincodeMethodName);

         LOG.info("Finished");
      }
      catch (final Exception e) {
         LOG.error("Error running", e);
      }
   }

   public void injectFromNode(final HyperLedgerNode node, final String chaincodeMethodName) {
      try {
         LOG.info("Starting");

         this.hyperLedgerNode = node;
         inject(createMessageBean(), chaincodeMethodName);

         LOG.info("Finished");
      }
      catch (final Exception e) {
         LOG.error("Error running", e);
      }
   }

   private MessageBean createMessageBean() {
      final MessageBean message = MessageBean.newInstance(UUID.randomUUID().toString(), 1, GSON.toJson(randomJson).getBytes());
      return message;
   }

   private void inject(final MessageBean bean, final String chaincodeMethod) {
      LOG.debug("Sending {}", bean);

      try {
         hyperLedgerNode.commitMessage(bean, chaincodeMethod);
      }
      catch (final Exception e) {
         LOG.error("Failed to send {}", bean, e);
      }

      LOG.debug("Send successful");
   }

}
