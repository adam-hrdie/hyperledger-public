package hm.constants;

import static hm.api.StartingBlock.CURRENT;

import hm.api.StartingBlock;

public class HyperledgerConstants {

   public static final String CERTIFICATE_PATH = "ca-certificates";

   // this can only be registered / enrolled once
   public static final String HF_USERNAME = "certificate.username"; //

   public static final StartingBlock STARTING_BLOCK = CURRENT;

   public static final int CATCHUP_BLOCKS_TIMEOUT = 10;

   //these are custom method / event names defined in the chaincode contracts (installed on peer0 & orderer)
   public static final String CHAINCODE_SEND_METHOD = "init_message"; //the name of the chaincode method to call
   public static final String CHAINCODE_ACK_METHOD = "ack_message"; //the name of the chaincode method to call
   public static final String CHAINCODE_SEND_EVENT = "message_processed"; //the name of the chaincode event to listen to in the resulting transaction
   public static final String CHAINCODE_ACK_EVENT = "message_response"; //the name of the chaincode event to listen to in the resulting transaction
   public static final String CHAINCODE_LBP = "last_block_processed"; //the name of the chaincode event to listen to in the resulting transaction

   private HyperledgerConstants() {
      // hiding constructor
   }

}