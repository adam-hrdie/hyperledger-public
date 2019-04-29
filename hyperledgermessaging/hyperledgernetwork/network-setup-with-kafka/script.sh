#set env
export CA_KEYFILE=`ls crypto-config/peerOrganizations/group/ca | sort -g | tail -n 1`

#make sure docker is running in swarm mode
docker swarm leave -f
docker swarm init

# KAFKA & ZOOKEEPER
docker stack deploy -c kafka-zookeeper/docker-compose-zookeeper.yaml nodex
sleep 10
docker stack deploy -c kafka-zookeeper/docker-compose-kafka.yaml nodex

#SLEEP
echo awaiting kafka containers startup...
sleep 60

# GMEX COMPONENTS
docker stack deploy -c group-organisation/docker-compose-orderer.yaml nodex
sleep 5
docker stack deploy -c group-organisation/docker-compose-services.yaml nodex
sleep 5
docker stack deploy -c group-organisation/docker-compose-peer.yaml nodex

#SLEEP
echo awaiting peer containers startup...
sleep 15

export CLI_NAME="$(docker ps --format='{{.Names}}' | grep _cli)"
export PEER_NAME="$(docker ps --format='{{.Names}}' | grep _peer0)"

#initialise channel on peers & orderers
docker exec $PEER_NAME peer channel create -o orderer0.group:7050 -c messagebus -f /etc/hyperledger/configtx/channel.tx

#OR IF channel has already been created and we want to join a new peer to the existing channel 
docker exec $PEER_NAME peer channel fetch config messagebus.block -c messagebus -o orderer0.group:7050 

#join the channel
docker exec $PEER_NAME peer channel join -b messagebus.block

#use the cli interface to list channels
docker exec $CLI_NAME peer channel list

#install default chaincode
docker exec $CLI_NAME peer chaincode install -n chainc -v 1.0 -p github.com/go -l golang

#instantiate default chaincode
docker exec $CLI_NAME peer chaincode instantiate -o orderer0.group:7050 -C messagebus -n chainc -l "golang" -v 1.0 -c '{"Args":[""]}' -P "OR ('Org1MSP.member','Org2MSP.member')"

