package hm.beans;

import hm.api.BlockReport;

public class BlockchainEventWrapper {

   private final BlockReport blockReport;

   private final MessageBean messageBean;

   /*
    * Wrap the messageBean and the blockEvent into a single class to pass to the processor
    * Enables block info getters for blockNumber, transactionEvents, etc
    */

   public BlockchainEventWrapper(final BlockReport blockReport, final MessageBean messageBean) {
      super();
      this.blockReport = blockReport;
      this.messageBean = messageBean;
   }

   public BlockReport blockReport() {
      return blockReport;
   }

   public MessageBean messageBean() {
      return messageBean;
   }

   public boolean hasBlockReport() {
      return blockReport != null;
   }

   public boolean hasMessageBean() {
      return messageBean != null;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((blockReport == null) ? 0 : blockReport.hashCode());
      result = prime * result + ((messageBean == null) ? 0 : messageBean.hashCode());
      return result;
   }

   @Override
   public boolean equals(final Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      final BlockchainEventWrapper other = (BlockchainEventWrapper) obj;
      if (blockReport == null) {
         if (other.blockReport != null)
            return false;
      }
      else if (!blockReport.equals(other.blockReport))
         return false;
      if (messageBean == null) {
         if (other.messageBean != null)
            return false;
      }
      else if (!messageBean.equals(other.messageBean))
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "BlockchainEventWrapper [blockReport=" + blockReport.toString() + ", messageBean=" + messageBean.toString() + "]";
   }

}
