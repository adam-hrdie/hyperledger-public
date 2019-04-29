package hm.injector;

import org.slf4j.*;

import com.google.gson.Gson;

import hm.node.*;

public abstract class HyperLedgerAbstractInjector {

   protected final Gson GSON = new Gson();

   protected HyperLedgerNode hyperLedgerNode;

   private static final Logger LOG = LoggerFactory.getLogger(HyperLedgerAbstractInjector.class);

   protected HyperLedgerNode init() throws Exception {
      LOG.debug("init start");

      hyperLedgerNode = new HyperLedgerNodeImpl("Injector");
      hyperLedgerNode.startWithDefaultUrl();

      LOG.debug("init end");
      return hyperLedgerNode;
   }

}
