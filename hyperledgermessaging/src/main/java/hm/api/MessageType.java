package hm.api;

import java.util.*;

import org.slf4j.*;

public enum MessageType {

   UNKNOWN(-1, null),
   LAST_BLOCK_PROCESSED(0, LastBlockProcessed.class), //this is required to be 0 for specific chaincode LBP call
   MESSAGE(1, null);
   
   private static final Logger LOG = LoggerFactory.getLogger(MessageType.class);

   private final int value;
   private final Class<? extends BlockChainMessage> messageObjectClass;

   private static final Map<Integer, MessageType> values = new HashMap<>();
   static {
      for (final MessageType message : MessageType.values()) {
         values.put(message.value, message);
      }
   }

   MessageType(final int value, final Class<? extends BlockChainMessage> messageObjectClass) {
      this.value = value;
      this.messageObjectClass = messageObjectClass;
   }

   public static MessageType fromInt(final int value) {
      final MessageType message = values.get(value);
      if (message == null) {
         LOG.warn("Could not convert int value: {}", value);
         return UNKNOWN;
      }
      return message;
   }

   public int getValue() {
      return this.value;
   }

   public Class<? extends BlockChainMessage> getMessageObjectClass() {
      return this.messageObjectClass;
   }
}