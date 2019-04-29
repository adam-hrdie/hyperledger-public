package hm.api;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory {

   private final String name;
   private final boolean daemon;

   public NamedThreadFactory(String name, boolean daemon) {
      this.name = name;
      this.daemon = daemon;
   }

   @Override
   public Thread newThread(Runnable r) {
      Thread t = new Thread(r, name);
      t.setDaemon(daemon);
      return t;
   }
}
