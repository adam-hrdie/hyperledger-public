package hm.node.auth;

import java.io.*;
import java.nio.file.*;

import hm.api.AppUserBean;
import hm.constants.HyperledgerConstants;

public class HyperLedgerSerializationTools {

   public void serializeUserToFile(final AppUserBean appUser) throws IOException {
      try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(HyperledgerConstants.CERTIFICATE_PATH + File.separator + appUser.getName() + ".jso")))) {
         oos.writeObject(appUser);
      }
   }

   public AppUserBean tryDeserializeUserFromFile(final String name) throws ClassNotFoundException, IOException {
      if (Paths.get(HyperledgerConstants.CERTIFICATE_PATH + File.separator + name + ".jso").toFile().exists()) {
         return deserializeUserFromFile(name);
      }
      return null;
   }

   public AppUserBean tryDeserializeUser(final String name) throws IOException, ClassNotFoundException {
      AppUserBean user = tryDeserializeUserFromFile(name);
      return user;
   }

   public AppUserBean deserializeUserFromFile(final String name) throws IOException, ClassNotFoundException {
      try (ObjectInputStream decoder = new ObjectInputStream(Files.newInputStream(Paths.get(HyperledgerConstants.CERTIFICATE_PATH + File.separator + name + ".jso")))) {
         return (AppUserBean) decoder.readObject();
      }
   }
}
