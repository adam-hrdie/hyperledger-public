package hm.thread;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.slf4j.*;

import hm.api.BlockReport;
import hm.api.MessageType;
import hm.beans.BlockchainEventWrapper;
import hm.beans.MessageBean;
import hm.node.HyperLedgerNode;

public class MessageProcessor implements Runnable {

	private MessageBean response;
	private final String name;
	private volatile boolean running = true;
	private final boolean shouldRespond;
	private final HyperLedgerNode node;
	private final BlockingQueue<BlockchainEventWrapper> queue;
	private static final Logger LOG = LoggerFactory.getLogger(MessageProcessor.class);

	public MessageProcessor(final String name, final BlockingQueue<BlockchainEventWrapper> queue,
			final HyperLedgerNode node, boolean shouldRespond) {
		this.queue = queue;
		this.node = node;
		this.name = name;
		this.shouldRespond = shouldRespond;
	}

	@Override
	public void run() {
		while (running) {
			try {
				processQueue();
			} catch (Exception e) {
				LOG.error("failed to process message", e);
			}
		}
	}

	void processQueue() throws Exception {
		try {
			final BlockchainEventWrapper be = queue.take();

			if (be != null && be.hasMessageBean())
				processAMessage(be.messageBean());

			if (be != null && be.hasBlockReport())
				publishLastBlockProcessed(be.blockReport());

			if (shouldRespond)
				sendAResponse();

		} catch (final InterruptedException e) {
			LOG.debug("Interrupted while waiting for a message on the queue - will retry whilst running = true");
		}
	}

	private void processAMessage(final MessageBean mb) {
		LOG.info("node [{}] received a message: [{}]", name, mb.toString());
	}

	private void publishLastBlockProcessed(final BlockReport blockReport) {
		try {
			node.commitLastBlockProcessed(blockReport);
		} catch (final Exception e) {
			LOG.error("Failed to commit lastBlockProcessed for blockReport {}", blockReport, e);
		}
	}

	private void sendAResponse() throws Exception {
		response = new MessageBean(UUID.randomUUID().toString(), MessageType.MESSAGE.getValue(), new byte[] { 'a' });
		node.commitMessageResponse(response);
	}
}