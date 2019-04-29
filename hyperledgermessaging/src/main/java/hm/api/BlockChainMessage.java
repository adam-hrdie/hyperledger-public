package hm.api;

import java.io.Serializable;

public abstract class BlockChainMessage implements Serializable {

   private static final long serialVersionUID = 20181126_1157L;

   protected String ledgerId = null;

   public String getLedgerId() {
      return ledgerId;
   }

   public void setLedgerId(final String id) {
      this.ledgerId = id;
   }

}
