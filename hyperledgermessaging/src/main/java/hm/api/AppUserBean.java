package hm.api;

import java.io.Serializable;
import java.util.Set;

import org.hyperledger.fabric.sdk.*;

/*
 * <h1>AppUser</h1>
 * <p>
 * Basic implementation of the {@link User} interface.
 */
public class AppUserBean implements User, Serializable {

   private static final long serialVersionUID = 20180621_115254L;

   private String name;
   private Set<String> roles;
   private String account;
   private String affiliation;
   private Enrollment enrollment;
   private String mspId;

   public AppUserBean(final String name, final String affiliation, final String mspId, final Enrollment enrollment) {
      this.name = name;
      this.affiliation = affiliation;
      this.enrollment = enrollment;
      this.mspId = mspId;
   }

   @Override
   public String getName() {
      return name;
   }

   @Override
   public Set<String> getRoles() {
      return roles;
   }

   @Override
   public String getAccount() {
      return account;
   }

   @Override
   public String getAffiliation() {
      return affiliation;
   }

   @Override
   public Enrollment getEnrollment() {
      return enrollment;
   }

   @Override
   public String getMspId() {
      return mspId;
   }

   @Override
   public String toString() {
      return "AppUser{" + "name='" + name + '\'' + "\n, roles=" + roles + "\n, account='" + account + '\'' + "\n, affiliation='" + affiliation + '\'' + "\n, enrollment="
            + enrollment + "\n, mspId='" + mspId + '\'' + '}';
   }
}
