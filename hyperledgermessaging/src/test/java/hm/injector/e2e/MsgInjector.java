package hm.injector.e2e;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hm.constants.HyperledgerConstants;
import hm.injector.InjectHyperLedgerMessage;

class MsgInjector extends InjectHyperLedgerMessage {

	private static final Logger LOG = LoggerFactory.getLogger(MsgInjector.class);
	String lineSeparator = System.getProperty("line.separator");

	@Test
	public void inject() {
		try {
			LOG.info("{}------------------------------------{}     About to inject a message{}------------------------------------{}", lineSeparator, lineSeparator, lineSeparator, lineSeparator);

			init();
			int i = 0;
			while (i < 1) {
				inject(createMessageBean(), HyperledgerConstants.CHAINCODE_SEND_METHOD);
				i++;
			}

			LOG.info("Finished");
		} catch (final Exception e) {
			LOG.error("Error running", e);
		}
	}
}
