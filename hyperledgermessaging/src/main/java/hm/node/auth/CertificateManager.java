package hm.node.auth;

import java.lang.reflect.InvocationTargetException;

import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.*;

import hm.api.AppUserBean;


public interface CertificateManager {

   public HFClient initialize() throws Exception;

   public HFClient getHfClient()
         throws IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException, InvocationTargetException;

   public AppUserBean getUser();

}
