/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package main

import (
	"encoding/json"
	"fmt"
    "crypto/sha1"
    "encoding/base64"

	"github.com/hyperledger/fabric/core/chaincode/shim"
	pb "github.com/hyperledger/fabric/protos/peer"
)

// ============================================================================================================================
// write() - genric write variable into ledger
// 
// Shows Off PutState() - writting a key/value into the ledger
//
// Inputs - Array of strings
//    0   ,    1
//   key  ,  value
//  "abc" , "test"
// ============================================================================================================================
func write(stub shim.ChaincodeStubInterface, args []string) pb.Response {
	var key, value string
	var err error
	fmt.Println("starting write")

	if len(args) != 2 {
		return shim.Error("Incorrect number of arguments. Expecting 2. key of the variable and value to set")
	}

	key = args[0]                                   //rename for funsies
	value = args[1]
	err = stub.PutState(key, []byte(value))         //write the variable into the ledger
	if err != nil {
		return shim.Error(err.Error())
	}

	fmt.Println("- end write")
	return shim.Success(nil)
}

// ============================================================================================================================
// Init Message - create a new message from json stream, store into chaincode state using GoLang structure
//
// Inputs - string json
//
// ============================================================================================================================
func init_message(stub shim.ChaincodeStubInterface, args []string) (pb.Response) {
	var err error
	fmt.Println("starting init_message")

    json_message_string := args[0]
    var message Message

    err = json.Unmarshal([]byte(json_message_string), &message)
    if err != nil {
		fmt.Println("Could not store message")
		return shim.Error(err.Error())
	}

	if(message.Type == 0) {
		return block_processed(stub, message)
	}
   	
	fmt.Println(message)

	//store message
	messageAsBytes, err := json.Marshal(message)                     //convert to array of bytes
	err = stub.PutState(message.Id, messageAsBytes)                //store message by its Id
	if err != nil {
		fmt.Println("Could not store message")
		return shim.Error(err.Error())
	}

	fmt.Println("- end init_message")

	stub.SetEvent("message_processed", []byte(json_message_string))

	return shim.Success(nil)
}

// ============================================================================================================================
// Publish response 
//
// ============================================================================================================================
func ack_message(stub shim.ChaincodeStubInterface, args []string) (pb.Response) {
	var err error
	fmt.Println("starting response_message")

    json_message_string := args[0]
    var message_ack Message

    err = json.Unmarshal([]byte(json_message_string), &message_ack)
    if err != nil {
		fmt.Println("Could not store message")
		return shim.Error(err.Error())
	}
   	
	fmt.Println(message_ack)

	//store message
	messageAsBytes2, err := json.Marshal(message_ack)                 //convert to array of bytes
	err = stub.PutState(message_ack.Id, messageAsBytes2)                //store message by its Id
	if err != nil {
		fmt.Println("Could not store message")
		return shim.Error(err.Error())
	}

	fmt.Println("- end clearer_response")

	//publish an event as part of the transaction in order to deliver the message to the subscribed peers
	//limited to one per transaction
	stub.SetEvent("message_response", []byte(json_message_string))
	return shim.Success(nil)
}

// ============================================================================================================================
// Record block processed event 
//
// ============================================================================================================================
func block_processed(stub shim.ChaincodeStubInterface, blockprocessed Message) (pb.Response) {
	var err error
	fmt.Println("recording last_block_processed")
   	
	id, err := stub.GetCreator()
	if err != nil {
		fmt.Println("Could not store message")
		return shim.Error(err.Error())
	}
	
	hasher := sha1.New()
    hasher.Write(id)
    sha := base64.URLEncoding.EncodeToString(hasher.Sum(nil))
    
	//store message
	messageAsBytes3, err := json.Marshal(blockprocessed)    //convert to array of bytes
	err = stub.PutState(sha, messageAsBytes3)               //store message by the user id
	if err != nil {
		fmt.Println("Could not store message")
		return shim.Error(err.Error())
	}

	fmt.Println("wrote lbp for user hash: [" + sha + "]")

	fmt.Println("- end block_processed")
	return shim.Success(nil)
}

