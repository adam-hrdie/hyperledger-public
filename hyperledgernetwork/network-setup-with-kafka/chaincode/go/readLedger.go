package main

import (
    "fmt"
	"errors"
	"strconv"
    "crypto/sha1"
    "encoding/base64"
	"encoding/json"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

// ============================================================================================================================
// Read - read a generic variable from ledger
//
// Shows Off GetState() - reading a key/value from the ledger
//
// Inputs - Array of strings
//  0
//  key
//  "abc"
// 
// Returns - string
// ============================================================================================================================
func read(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var key, jsonResp string
	var err error
	fmt.Println("starting read")

	if len(args) != 1 {
		return shim.Error("Incorrect number of arguments. Expecting key of the var to query")
	}

	// input sanitation
	err = sanitize_arguments(args)
	if err != nil {
		return shim.Error(err.Error())
	}

	key = args[0]
	valAsbytes, err := stub.GetState(key)           //get the var from ledger
	if err != nil {
		jsonResp = "{\"Error\":\"Failed to get state for " + key + "\"}"
		return shim.Error(jsonResp)
	}

	fmt.Println("- end read")
	return shim.Success(valAsbytes)                  //send it onward
}

func sanitize_arguments(strs []string) error{
	for i, val:= range strs {
		if len(val) <= 0 {
			return errors.New("Argument " + strconv.Itoa(i) + " must be a non-empty string")
		}
		if len(val) > 32 {
			return errors.New("Argument " + strconv.Itoa(i) + " must be <= 32 characters")
		}
	}
	return nil
}

func last_block_processed(stub shim.ChaincodeStubInterface) pb.Response {

	id, err := stub.GetCreator()
	
	if err != nil {
		fmt.Println("Could not store message")
		return shim.Error(err.Error())
	}
	
	hasher := sha1.New()
    hasher.Write(id)
    sha := base64.URLEncoding.EncodeToString(hasher.Sum(nil))
    
	fmt.Println("read lbp for user hash: [" + sha + "]")
	
	messageAsBytes, _ := stub.GetState(sha)
    var message Message
    
    err = json.Unmarshal(messageAsBytes, &message)
    if err != nil {
		fmt.Println("Could not store message")
		return shim.Success(messageAsBytes)
	}

	fmt.Println(message)
	
	return shim.Success(messageAsBytes)
}

