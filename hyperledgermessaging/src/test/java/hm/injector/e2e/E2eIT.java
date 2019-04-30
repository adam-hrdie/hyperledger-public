package hm.injector.e2e;

import java.util.UUID;
import java.util.concurrent.*;

import org.hyperledger.fabric.sdk.ChaincodeEventListener;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.junit.jupiter.api.Test;
import org.slf4j.*;

import hm.api.MessageType;
import hm.api.NamedThreadFactory;
import hm.beans.BlockchainEventWrapper;
import hm.beans.MessageBean;
import hm.constants.HyperledgerConstants;
import hm.injector.InjectHyperLedgerMessage;
import hm.listener.ChaincodeEventListenerImpl;
import hm.node.*;
import hm.thread.MessageProcessor;

public class E2eIT {
	private static final Logger LOG = LoggerFactory.getLogger(E2eIT.class);

	private static HyperLedgerNode node1;
	private static HyperLedgerNode node2;

	final BlockingQueue<BlockchainEventWrapper> node1Processingqueue = new ArrayBlockingQueue<>(100);
	final BlockingQueue<BlockchainEventWrapper> node2Processingqueue = new ArrayBlockingQueue<>(100);
	
	ExecutorService executorServiceNode1;
	ExecutorService executorServiceNode2;

	@Test
	public void startTest() throws Exception {
		/*
		 * Node1 calls chaincode init_message to create chaincode events called
		 * "message_processed" Node2 calls chaincode ack_message to create chaincode
		 * events called "message_response"
		 *
		 * Creating up and down stream directionary message flow between the nodes.
		 */
		
		setupNodes();

		/*
		 * Thread 1: Node1 listens for chaincode events and logs them. Node1 commits a
		 * random message to the channel of type "init_message".
		 *
		 * Thread 2: Node2 listens for chaincode events, logs them, and then responds
		 * with an acknowledgement. Node2 commits random messages, only when it receives
		 * and successfully processes a message from Node1.
		 *
		 * After the first sucessful round trip, node1 waits for a specified time, and
		 * tries again. If this time is > 15 minutes, the message will fail to be
		 * "successfully committed" by Node1 as a DeliverResponse is never returned by
		 * the Ordering Service Node.
		 */
		
		while(true)
			;
	}

	private void commitRandomUpsteamMessage(HyperLedgerNode node) throws Exception {
		MessageBean bean = new MessageBean(UUID.randomUUID().toString(), MessageType.MESSAGE.getValue(),
				InjectHyperLedgerMessage.randomJson.getBytes());
		node1.commitMessage(bean, HyperledgerConstants.CHAINCODE_SEND_METHOD);
	}

	private void setupNodes() throws Exception {
		node1 = new HyperLedgerNodeImpl("node1");
		node2 = new HyperLedgerNodeImpl("node2");

		node1.startWithDefaultUrl();
		node2.startWithDefaultUrl();

		startListenerThreads();
		Thread.sleep(1000L);
		
		subscribeToChaincodeEvents();
	}

	private void subscribeToChaincodeEvents() throws InvalidArgumentException {
		connectChainCodeListener(HyperledgerConstants.CHAINCODE_ACK_EVENT, node1Processingqueue, node1);
		connectChainCodeListener(HyperledgerConstants.CHAINCODE_SEND_EVENT, node2Processingqueue, node2);
	}

	private void connectChainCodeListener(final String eventName, final BlockingQueue<BlockchainEventWrapper> queue,
			final HyperLedgerNode node) throws InvalidArgumentException {
		final ChaincodeEventListener eventListener = new ChaincodeEventListenerImpl(queue);
		node.subscribeToChannel(eventListener, eventName);
	}
	
	private void startListenerThreads() {
		executorServiceNode1 = Executors.newSingleThreadExecutor(new NamedThreadFactory("node1Processing", false));
		executorServiceNode1.execute(new MessageProcessor(node1.name(), node1Processingqueue, node1, false));

		executorServiceNode2 = Executors.newSingleThreadExecutor(new NamedThreadFactory("node2Processing", false));
		executorServiceNode2.execute(new MessageProcessor(node2.name(), node2Processingqueue, node2, true));
	}
}
