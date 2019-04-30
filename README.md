# hyperledger-public

## Overview
This repository allows a user to:

1. create a sample Hyperledger network using hyperledgernetwork project.
2. run a two-node end to end test against the network.

The test is intended to show issues occurring after long wait periods between commits on a multi-orderer (kafka-based) hyperledger. 


## Prerequisites: 
* CentOS Linux release 7.6.1810 (Core) 
* Docker version 18.09.3, build 774a1f4
* [Hyperledger prerequisites](https://hyperledger-fabric.readthedocs.io/en/release-1.4/prereqs.html)
* Latest Hyperledger image tags 

## Installation
### Hyperledger

1. Pull the hyperledgernetwork project to the desired install location. Set all .sh files and everything in /bin to executable.
2. Ensure no Hyperledger containers or networks are currently running on the machine - docker network prune / docker volume prune.
3. Run the script.sh file. Usually I manually run line by line to ensure each docker service has time to initialise and start (once it is shown as running in docker ps, continue) 

### Java

1. Pull the hyperledgermessaging project into your IDE. 
2. Ensure project is set to run on at least JDK 1.8.0_111
3. Run the [Junit Test](https://github.com/adam-hrdie/hyperledger-public/blob/master/hyperledgermessaging/src/test/java/hm/injector/e2e/E2eIT.java) 
4. Ensure that the logs show two clients successfully connecting to the Hyperledger network & chaincode event listener
5. Ensure that the message throughput works by checking the logs - we should see activity on both nodes as they receive messages.
     (Optional) Run the [MsgInjector](https://github.com/adam-hrdie/hyperledger-public/blob/master/hyperledgermessaging/src/test/java/hm/injector/e2e/MsgInjector.java) to inject a message and show the round trip between the nodes. 
6. Wait for 16 minutes
7. After this time, the junit test should fail to send a message to the 2nd node with a timeout exception.




