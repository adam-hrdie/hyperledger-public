package hm.api;


public class LastBlockProcessed extends BlockChainMessage {

   private static final long serialVersionUID = 20181108_1138L;

   private final long blockNumber;

   public LastBlockProcessed(final long blockNumber) {
      this.blockNumber = blockNumber;
   }

   public LastBlockProcessed(final BlockReport blockReport) {
      if (blockReport != null)
         this.blockNumber = blockReport.number();
      else
         throw new IllegalArgumentException("LastBlockProcessed : BlockReport is null");
   }

   public long getBlockNumber() {
      return blockNumber;
   }

   @Override
   public String toString() {
      return "LastBlockProcessed [blockNumber=" + blockNumber + "]";
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (blockNumber ^ (blockNumber >>> 32));
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
      final LastBlockProcessed other = (LastBlockProcessed) obj;
      if (blockNumber != other.blockNumber)
         return false;
      return true;
   }

}
