package hm.injector.e2e;

import java.util.UUID;
import java.util.concurrent.*;

import org.hyperledger.fabric.sdk.ChaincodeEventListener;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	String lineSeparator = System.getProperty("line.separator");
	
	private static final String node1Name = "NODE1";
	private static final String node2Name = "NODE2";
	private static final String node3Name = "NODE3";
	private static final String node4Name = "NODE4";

	private static HyperLedgerNode node1;
	private static HyperLedgerNode node2;
	private static HyperLedgerNode node3;
	private static HyperLedgerNode node4;

	ExecutorService executorServiceNode1;
	ExecutorService executorServiceNode2;
	ExecutorService executorServiceNode3;
	ExecutorService executorServiceNode4;

	final BlockingQueue<BlockchainEventWrapper> node1Processingqueue = new ArrayBlockingQueue<>(100);
	final BlockingQueue<BlockchainEventWrapper> node2Processingqueue = new ArrayBlockingQueue<>(100);
	final BlockingQueue<BlockchainEventWrapper> node3Processingqueue = new ArrayBlockingQueue<>(100);
	final BlockingQueue<BlockchainEventWrapper> node4Processingqueue = new ArrayBlockingQueue<>(100);


	@Test
	public void startTest() throws Exception {
		/*
		 * Node1 calls chaincode init_message to create chaincode events called
		 * "message_processed" Node2 calls chaincode ack_message to create chaincode
		 * events called "message_response"
		 * 
		 * Node 3 and 4 listen to all up/down stream messages, and just log without responding.
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
		 * "successfully committed" by Node1 as the connection is reset by Peer.
		 */

		//commit a message
		commitRandomUpsteamMessage(node1);
		

		long timeToWait = 1000 * 60 * 16; // 16 minutes
		busyWait(timeToWait);
		
		//try to commit another message
		commitRandomUpsteamMessage(node1);
		
		while (true) {
			; // run forever while we wait for the acknowledgement from node2
		}
	}

	private void busyWait(long timeToWait) {
		long timeout = System.currentTimeMillis() + timeToWait;
		while (System.currentTimeMillis() < timeout) {
			;
		}
	}

	private void commitRandomUpsteamMessage(HyperLedgerNode node) throws Exception {
		MessageBean bean = new MessageBean(UUID.randomUUID().toString(), MessageType.MESSAGE.getValue(),
				InjectHyperLedgerMessage.randomJson.getBytes(), System.currentTimeMillis());

		LOG.info("{}------------------------------------{}     About to inject a message{}------------------------------------{}", lineSeparator, lineSeparator, lineSeparator, lineSeparator);
		node1.commitMessage(bean, HyperledgerConstants.CHAINCODE_SEND_METHOD);
	}

	private void setupNodes() throws Exception {
		node1 = new HyperLedgerNodeImpl(node1Name);
		node2 = new HyperLedgerNodeImpl(node2Name);
		node3 = new HyperLedgerNodeImpl(node3Name);
		node4 = new HyperLedgerNodeImpl(node4Name);

		node1.startWithDefaultUrl(node1.name());
		node2.startWithDefaultUrl(node2.name());
		node3.startWithDefaultUrl(node3.name());
		node4.startWithDefaultUrl(node4.name());

		startListenerThreads();
		Thread.sleep(1000L);

		subscribeToChaincodeEvents();
	}

	private void subscribeToChaincodeEvents() throws InvalidArgumentException {
		connectChainCodeListener(HyperledgerConstants.CHAINCODE_ACK_EVENT, node1Processingqueue, node1);
		connectChainCodeListener(HyperledgerConstants.CHAINCODE_SEND_EVENT, node2Processingqueue, node2);
		
		connectChainCodeListener(HyperledgerConstants.CHAINCODE_ACK_EVENT, node3Processingqueue, node3);
		connectChainCodeListener(HyperledgerConstants.CHAINCODE_SEND_EVENT, node3Processingqueue, node3);
		connectChainCodeListener(HyperledgerConstants.CHAINCODE_ACK_EVENT, node4Processingqueue, node4);
		connectChainCodeListener(HyperledgerConstants.CHAINCODE_SEND_EVENT, node4Processingqueue, node4);
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
		
		//below threads just listen and dont do anything with the messages apart from logging.
		executorServiceNode3 = Executors.newSingleThreadExecutor(new NamedThreadFactory("node3Processing", false));
		executorServiceNode3.execute(new MessageProcessor(node3.name(), node2Processingqueue, node3, false));
		executorServiceNode4 = Executors.newSingleThreadExecutor(new NamedThreadFactory("node4Processing", false));
		executorServiceNode4.execute(new MessageProcessor(node4.name(), node4Processingqueue, node4, false));
	}
}
