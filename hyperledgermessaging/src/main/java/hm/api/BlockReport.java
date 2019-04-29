package hm.api;

import java.util.Arrays;

public class BlockReport {

   private final long number;

   private final byte[] hash;

   public BlockReport(final long number, final byte[] hash) {
      super();
      this.number = number;
      this.hash = hash;
   }

   public long number() {
      return number;
   }

   public byte[] hash() {
      return hash;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(hash);
      result = prime * result + (int) (number ^ (number >>> 32));
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
      final BlockReport other = (BlockReport) obj;
      if (!Arrays.equals(hash, other.hash))
         return false;
      if (number != other.number)
         return false;
      return true;
   }

   @Override
   public String toString() {
      return "BlockReport [number=" + number + ", hash=" + new String(hash) + "]";
   }

}
