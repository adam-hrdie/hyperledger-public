package hm.injector.e2e;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hm.constants.HyperledgerConstants;
import hm.injector.InjectHyperLedgerMessage;

class MsgInjector extends InjectHyperLedgerMessage {

	private static final Logger LOG = LoggerFactory.getLogger(MsgInjector.class);

	@Test
	public void inject() {
		try {
			LOG.info("Injecting");

			init();
			inject(createMessageBean(), HyperledgerConstants.CHAINCODE_SEND_METHOD);

			LOG.info("Finished");
		} catch (final Exception e) {
			LOG.error("Error running", e);
		}
	}
}
