package hm.node.auth;

import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric_ca.sdk.*;
import org.hyperledger.fabric_ca.sdk.exception.RegistrationException;
import org.slf4j.*;

import hm.api.AppUserBean;
import hm.constants.HyperledgerConstants;
import hm.node.ConnectionProperties;

public class CertificateManagerImpl implements CertificateManager {

   private static final Logger LOG = LoggerFactory.getLogger(CertificateManagerImpl.class);

   private static AppUserBean user;
   private final HFCAClient caClient;
   private final HyperLedgerSerializationTools serializationTools;

   public CertificateManagerImpl() throws Exception {
      caClient = getHfCaClient(ConnectionProperties.HL_CA, null);
      serializationTools = new HyperLedgerSerializationTools();
   }

   public CertificateManagerImpl(final HFCAClient caClient, final HyperLedgerSerializationTools serializationTools) throws Exception {
      this.caClient = caClient;
      this.serializationTools = serializationTools;
   }

   @Override
   public HFClient initialize() throws Exception {
      LOG.info("Attempting connect to {}", ConnectionProperties.HL_CA);

      HFClient client = null;

      user = serializationTools.tryDeserializeUser(HyperledgerConstants.HF_USERNAME);
      if (user == null) {
         final AppUserBean admin = getAdmin(caClient);
         user = getUser(caClient, admin, HyperledgerConstants.HF_USERNAME);
      }

      client = getHfClient();
      client.setUserContext(user);

      return client;
   }

   /**
    * Get new fabic-ca client
    *
    * @param caUrl
    *           The fabric-ca-server endpoint url
    * @param caClientProperties
    *           The fabri-ca client properties. Can be null.
    * @return new client instance. never null.
    * @throws Exception
    */
   private HFCAClient getHfCaClient(final String caUrl, final Properties caClientProperties) throws Exception {
      final CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
      final HFCAClient caClient = HFCAClient.createNewInstance(caUrl, caClientProperties);
      caClient.setCryptoSuite(cryptoSuite);
      return caClient;
   }

   /**
    * Create new HLF client
    *
    * @return new HLF client instance. Never null.
    * @throws InvocationTargetException
    * @throws NoSuchMethodException
    * @throws ClassNotFoundException
    * @throws InstantiationException
    * @throws IllegalAccessException
    * @throws CryptoException
    * @throws InvalidArgumentException
    */
   @Override
   public HFClient getHfClient() throws IllegalAccessException, InstantiationException, ClassNotFoundException, CryptoException, InvalidArgumentException, NoSuchMethodException,
         InvocationTargetException {
      // initialize default cryptosuite
      final CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
      // setup the client
      final HFClient client = HFClient.createNewInstance();
      client.setCryptoSuite(cryptoSuite);

      LOG.info("getHfClient : {}", client);
      return client;
   }

   /**
    * Register and enroll user with userId. If AppUser object with the name
    * already exist on fs it will be loaded and registration and enrollment
    * will be skipped.
    *
    * @param caClient
    *           The fabric-ca client.
    * @param registrar
    *           The registrar to be used.
    * @param userId
    *           The user id.
    * @return AppUser instance with userId, affiliation,mspId and enrollment
    *         set.
    * @throws Exception
    */
   private AppUserBean getUser(final HFCAClient caClient, final AppUserBean registrar, final String userId) throws Exception {
      AppUserBean appUser = serializationTools.tryDeserializeUser(userId);

      if (appUser == null) {
         try {
            final Enrollment enrollment;
            final RegistrationRequest rr = new RegistrationRequest(userId, "org1");
            final String enrollmentSecret = caClient.register(rr, registrar);
            enrollment = caClient.enroll(userId, enrollmentSecret);
            appUser = new AppUserBean(userId, "org1", "Org1MSP", enrollment);
            serializationTools.serializeUserToFile(appUser);
         }
         catch (final RegistrationException e) {
            LOG.error("Registration for user failed. Existing user on CA has no local certificate files. Have they been moved / deleted?");
            throw new Exception("Unable to register to Hyperledger. You may need to alter component properties to use new username");
         }
      }

      LOG.info("getUser: {}", appUser);

      return appUser;
   }

   /**
    * Enroll admin into fabric-ca using {@code admin/adminpw} credentials. If
    * AppUser object already exist serialized on fs it will be loaded and new
    * enrollment will not be executed.
    *
    * @param caClient
    *           The fabric-ca client
    * @return AppUser instance with userid, affiliation, mspId and enrollment
    *         set
    * @throws Exception
    */
   private AppUserBean getAdmin(final HFCAClient caClient) throws Exception {
	 //Note : uses defaults as per Hyperledger-samples
      AppUserBean admin = serializationTools.tryDeserializeUser("admin"); 
      if (admin == null) {
         final Enrollment adminEnrollment = caClient.enroll("admin", "adminpw"); 
         admin = new AppUserBean("admin", "org1", "Org1MSP", adminEnrollment);
         serializationTools.serializeUserToFile(admin);
      }

      LOG.info("getAdmin: {}", admin);

      return admin;
   }

   @Override
   public AppUserBean getUser() {
      return user;
   }

}
