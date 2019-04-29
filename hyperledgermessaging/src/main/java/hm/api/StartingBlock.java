package hm.api;

public enum StartingBlock {

   GENESIS(0),
   CURRENT(1),
   LAST_PROCESSED(2);

   private final int value;

   StartingBlock(final int value) {
      this.value = value;
   }

   public int getValue() {
      return value;
   }

}
