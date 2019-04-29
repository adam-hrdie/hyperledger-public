package hm.node;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class ConnectionProperties {

   public static final String IP;
   public static final String CHANNEL;
   public static final String CHAINCODE;

   public static final int CA_PORT;

   public static final int PEER0_PORT;
   public static final String PEER0_NAME;

   public static final int ORDERER_PORT;
   public static final String ORDERER_NAME;

   public static final String HTTP_STUB;
   public static final String GRPC_STUB;

   public static final String HL_CA;
   public static final String HL_PEER0;
   public static final String HL_ORDERER;

   static {

      IP = "gmex-dev-dlt-01";
      CHANNEL = "messagebus";
      CHAINCODE = "chainc";

      CA_PORT = 7054;
      PEER0_PORT = 7051;
      ORDERER_PORT = 7050;
      PEER0_NAME = "peer0.group";
      ORDERER_NAME = "orderer0.group";

      HTTP_STUB = "http://" + IP + ":";
      GRPC_STUB = "grpc://" + IP + ":";

      HL_CA = HTTP_STUB + CA_PORT;
      HL_PEER0 = GRPC_STUB + PEER0_PORT;
      HL_ORDERER = GRPC_STUB + ORDERER_PORT;
   }

   public static Properties getOrdererProperties() {
      final Properties ordererProperties = new Properties();
      //avoid timeouts on inactive http2 connections.
      ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] { 5L, TimeUnit.MINUTES });
      ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] { 8L, TimeUnit.SECONDS });
      ordererProperties.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[] { true });
      return ordererProperties;
   }

}
