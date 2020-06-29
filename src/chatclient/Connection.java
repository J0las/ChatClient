/*
 *  Copyright (C) 2020  Felix Johannsmann, Johan Bücker
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, version 3, as published by
 *  the Free Software Foundation.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

/*
 * Copyright (c) 1997, 2017, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle nor the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 */ 

package chatclient;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import chatclient.lib.ChatMagicNumbers;
import chatclient.lib.ConnectionError;
import chatclient.lib.Constants;
import chatclient.lib.Crypto;
import chatclient.lib.CryptoModes;
import chatclient.lib.Hash;
import chatclient.lib.HashModes;
import chatclient.lib.Panic;
import chatclient.log.Log;
import chatclient.log.LogType;

public class Connection{
	/*Socket for connection to the other ChatClient*/
	private Socket 		socket;
	/*printstream for easy output*/
	private PrintStream out;
	/*Name of the other ChatCLient*/
	private String 		otherName	= "";
	/*Name of this
	 *  ChatCLient*/
	private String		ownName = "";
	/*scanner for easy input*/
	private Scanner 	sc;
	/*Represents the state of the connection*/
	private boolean 	closed 	= false;
	/*Hashobject  for creating or validating hashes*/
	private Hash 		hash;
	/*Cryptoobject for encrypting and decrypting messages and hashes*/
	private Crypto 		crypto;
	Connection(Socket socket,String ownName,boolean openedConnection) 
				throws ConnectionError{
		this.hash = new Hash(this);
		this.socket=socket;
		this.ownName = ownName;
		try {
			/*Create a printstream for easy output*/
			this.out = new PrintStream(socket.getOutputStream(),true,StandardCharsets.UTF_8);
			/*Create an scanner for easy input*/
			sc = new Scanner(socket.getInputStream(),StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*Branch if the connection was opened from this ChatClient*/
		if(openedConnection){
			/*Sends the first header*/
			sendHeader(ChatMagicNumbers.HELLO);
			/*Checks if the response is valid*/
			checkHeader(ChatMagicNumbers.ACCEPTED_CONNECTION);
			/*Sends the own name to the other ChatClient*/
		/*Branch if the connection was opened from the other ChatClient*/
		}else {
			/*Checks if the first Message is valid*/
			checkHeader(ChatMagicNumbers.HELLO);
			/*Send the Accepted Connection Header*/
			sendHeader(ChatMagicNumbers.ACCEPTED_CONNECTION);
			/*Receives the name of the other ChatClient*/
		}
		System.out.println(ownName+" exchanging keys");
		keyExchange(openedConnection);
		System.out.println(ownName+" checking keys");
		checkEncryption(openedConnection);
		System.out.println(ownName+" checking keys finished");
		if(openedConnection) {
			sendName();
			/*Receives the name of the other ChatClient*/
			recieveName();
		} else {
			/*Receives the name of the other ChatClient*/
			recieveName();
			/*Sends his own Name to the other ChatCllient*/
			sendName();
		}
		System.out.println(ownName+" finished");
		Log.log(new String[] {
				getIP_PORT(),
				otherName
		}, LogType.CONNECTION_ESTABLISHED);
	}
	/*Return true if there is a new Message*/
	boolean hasNewMessage(){
		return sc.hasNextLine();
	}
	/*Returns the content of the next Message*/
	String getNewMessage() throws ConnectionError{
		Panic.R_UNLESS(sc.hasNextLine(), "No new message".getBytes(), this);
		byte[] rawMessage = Base64.getDecoder().decode(sc.nextLine().getBytes(StandardCharsets.UTF_8));
		Panic.R_UNLESS(rawMessage[Constants.HEADER_OFFSET] == ChatMagicNumbers.ENC_MESSAGE, rawMessage, this);
		byte[] decData = crypto.cryptoOperation(rawMessage, CryptoModes.DECRYPT);
		byte[] forHash = new byte[decData.length+Constants.HEADER_SIZE];
		System.arraycopy(decData, 0, forHash, Constants.CHECKSUM_OFFSET, decData.length);
		hash.hash(forHash, HashModes.VALIDATE_HASH);
		return new String(Arrays.copyOfRange(forHash, Constants.MESSAGE_OFFSET, forHash.length),StandardCharsets.UTF_8);
	}
	/*Send a message to the other ChatCLient*/
	void sendMessage(String message) {
		byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
		byte[] sha = hash.hash(messageBytes, HashModes.CREATE_HASH);
		byte[] shaMessage = new byte[Constants.CHECKSUM_SIZE+messageBytes.length];
		System.arraycopy(sha, 0, shaMessage, 0, sha.length);
		System.arraycopy(messageBytes, 0, shaMessage, Constants.MESSAGE_OFFSET-1, messageBytes.length);
		shaMessage = crypto.cryptoOperation(shaMessage, CryptoModes.ENCRYPT);
		byte[] completeMessage = new byte[Constants.HEADER_SIZE+shaMessage.length];
		completeMessage[Constants.HEADER_OFFSET] = ChatMagicNumbers.ENC_MESSAGE;
		System.arraycopy(shaMessage, 0, completeMessage, Constants.CHECKSUM_OFFSET, shaMessage.length);
		out.println(new String(Base64.getEncoder().encode(shaMessage),StandardCharsets.UTF_8));
	}
	/*Sends the name of this ChatClient to the other ChatClient*/
	private void sendName() throws ConnectionError {
		byte[] nameBytes = this.ownName.getBytes(StandardCharsets.UTF_8);
		/*Allocate a buffer for the hash and name to be encrypted*/
		byte[] toEnc = new byte[Constants.HEADER_SIZE+Constants.CHECKSUM_SIZE+nameBytes.length];
		/*Copy the hash into the buffer*/
		System.arraycopy(
				hash.hash(
						nameBytes,HashModes.CREATE_HASH),
				0, toEnc, Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
		/*Copy the name in UTF-8 encoding into the buffer*/
		System.arraycopy(nameBytes, 0, toEnc, Constants.MESSAGE_OFFSET, nameBytes.length);
		/*Encrypt the buffer except for the first placeholder byte*/
		byte[] enc = crypto.cryptoOperation(toEnc, CryptoModes.ENCRYPT);
		/*Allocate a buffer for the complete message*/
		byte[] message = new byte[Constants.HEADER_SIZE+enc.length];
		/*Setting the headerbyte*/
		message[Constants.HEADER_OFFSET] = ChatMagicNumbers.CONNECTION_NAME;
		/*Copy the encrypted hash and name into the buffer*/
		System.arraycopy(enc, 0, message, Constants.CHECKSUM_OFFSET, enc.length);
		/*Send the name as a Base64 encoded String*/
		out.println(new String(Base64.getEncoder().encode(message),StandardCharsets.UTF_8));
	}
	/*Recieves the name of the other ChatClient and stores it in otherName*/
	private void recieveName() throws ConnectionError{
		/*Wait for the next message*/
		while(!sc.hasNextLine());
		/*Decode the incoming message from Base64 to raw bytes*/
		byte[] rawMessage =Base64.getDecoder().decode(sc.nextLine().getBytes(StandardCharsets.UTF_8));
		/*Check if the headerbyte is valid*/
		Panic.R_UNLESS(rawMessage[Constants.HEADER_OFFSET] == ChatMagicNumbers.CONNECTION_NAME,rawMessage, this);
		/*Decrypt the Message*/
		byte[] dec = crypto.cryptoOperation(rawMessage, CryptoModes.DECRYPT);
		/*Validate the hash*/
		hash.hash(dec, HashModes.VALIDATE_HASH);
		/*Store the name of the other chatclient*/
		this.otherName = new String(Arrays.copyOfRange(dec, Constants.MESSAGE_OFFSET, dec.length),StandardCharsets.UTF_8);
	}
	/*Returns true if the connection was closed*/
	boolean isClosed() {
		/*if it was already closed return true*/
		if(closed) return true;
		/*if the socket is closed set closed to true*/
		if(socket.isClosed()) closed = true;
		/*Return the value of closed*/
		return closed;
	}
	/*Closes the socket*/
	void closeConnection() {
		try {
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Log.log(new String[] {
				socket.getInetAddress().getHostAddress()+" / "+socket.getPort()
		}, LogType.CONNECTION_CLOSED);
		/*sets the closed flag to true*/
		closed = true;
	}
	/*Send the specified header*/
	private void sendHeader(byte header){
		out.println(new String(Base64.getEncoder().encode(new byte[] {header}),
								StandardCharsets.UTF_8));	
	}
	/*Check if the header matches the specified magic*/
	private void checkHeader(byte header) throws ConnectionError{
		/*Wait for the response*/
		while(!sc.hasNextLine());
		/*Check if the response contains the expected value*/
		byte[] response = Base64.getDecoder().decode(sc.nextLine());
		Panic.R_UNLESS(response[Constants.HEADER_OFFSET] == header,response,this);
	}
	private byte extractHeader() {
		/*Wait for the response*/
		while(!sc.hasNextLine());
		/*Check if the response contains the expected value*/
		byte[] response = Base64.getDecoder().decode(sc.nextLine());
		return response[Constants.HEADER_OFFSET];
	}
	private void keyExchange(boolean opened) throws ConnectionError {
		try {
			byte[] 	sharedSecret = new byte[0];
			SecretKeySpec			AES_Key;		
			if(opened) {
				KeyPairGenerator 	keyGen 			= KeyPairGenerator.getInstance("DH");
					keyGen.initialize(Constants.DH_KEY_SIZE);
				KeyPair 			keyPair 		= keyGen.generateKeyPair();
				KeyAgreement 		keyAgree 		= KeyAgreement.getInstance("DH");
									keyAgree.init(keyPair.getPrivate());
					sendPubKey(keyPair);
				KeyFactory			keyFactory		= KeyFactory.getInstance("DH");
				X509EncodedKeySpec	x509KeySpec		= new X509EncodedKeySpec(recievePubKey());
				PublicKey			otherPubKey		= keyFactory.generatePublic(x509KeySpec);
									keyAgree.doPhase(otherPubKey, true);
									sharedSecret	= keyAgree.generateSecret();
					AES_Key 		= new SecretKeySpec(sharedSecret, 0, Constants.AES_KEY_LENGTH, "AES");
				byte[] 				encodedAES_Params	= setUpCrypto(AES_Key);
					sendEncodedParams(encodedAES_Params);
									this.crypto = new Crypto(AES_Key, encodedAES_Params);
									
			}else {
				KeyFactory 			keyFactory 		= KeyFactory.getInstance("DH");
				X509EncodedKeySpec 	x509KeySpec 	= new X509EncodedKeySpec(recievePubKey());
				PublicKey			otherPubKey		= keyFactory.generatePublic(x509KeySpec);
				DHParameterSpec		otherDHParams	= ((DHPublicKey)otherPubKey).getParams();
				KeyPairGenerator	keyPairGen		= KeyPairGenerator.getInstance("DH");
									keyPairGen.initialize(otherDHParams);
				KeyPair				keyPair			= keyPairGen.generateKeyPair();
				KeyAgreement		keyAgree		= KeyAgreement.getInstance("DH");
									keyAgree.init(keyPair.getPrivate());
					sendPubKey(keyPair);
									keyAgree.doPhase(otherPubKey, true);
									sharedSecret	= keyAgree.generateSecret();
					AES_Key							= new SecretKeySpec(sharedSecret, 0, Constants.AES_KEY_LENGTH, "AES");			
									this.crypto = new Crypto(AES_Key, recieveEncodedParams());
			}
			Log.log(new String[] {
					getIP_PORT(),
					null,
					ownName,
					new String(Base64.getEncoder().encode(hash.hash(AES_Key.getEncoded(),HashModes.CREATE_HASH)),StandardCharsets.UTF_8)
			}, LogType.AES_KEY_HASH);
		}catch(IOException | InvalidKeyException | InvalidAlgorithmParameterException | InvalidKeySpecException e) {
			throw new ConnectionError("AES KEYEXCHANGE FAILED!".getBytes(StandardCharsets.UTF_8),this);
		}catch(NoSuchAlgorithmException e) {
			throw new AssertionError();
		}
	}
	private void sendEncodedParams(byte[] encodedAES_Params) throws ConnectionError{
		byte[] 	message		= new byte[Constants.HEADER_SIZE+Constants.CHECKSUM_SIZE+encodedAES_Params.length];
				message[Constants.HEADER_OFFSET]	
						= ChatMagicNumbers.ENCODED_PARAMS;
				System.arraycopy(hash.hash(encodedAES_Params, HashModes.CREATE_HASH), 0, message, Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
				System.arraycopy(encodedAES_Params, 0, message, Constants.MESSAGE_OFFSET, encodedAES_Params.length);
				out.println(new String(Base64.getEncoder().encode(message),StandardCharsets.UTF_8));
		
	}
	private byte[] recieveEncodedParams() throws ConnectionError {
		while(!sc.hasNextLine());
		byte[] message = Base64.getDecoder().decode(sc.nextLine().getBytes(StandardCharsets.UTF_8));
		Panic.R_UNLESS(message[Constants.HEADER_OFFSET]==ChatMagicNumbers.ENCODED_PARAMS, message, this);
		hash.hash(message, HashModes.VALIDATE_HASH);
		return Arrays.copyOfRange(message, Constants.MESSAGE_OFFSET, message.length);
	}
	/*Sends the PublicKey of this ChatClient to the other ChatClient*/
	void sendPubKey(KeyPair keyPair) throws ConnectionError {
		/*Gets the encoded PublicKey*/
		byte[] 	pubKeyBytes 	= keyPair.getPublic().getEncoded();
		/*Creates a buffer for the message*/
		byte[] 	messageBytes 	= new byte[pubKeyBytes.length+Constants.HEADER_SIZE+Constants.CHECKSUM_SIZE];
		/*Writes the headerbyte into the buffer*/
				messageBytes[Constants.HEADER_OFFSET] = ChatMagicNumbers.PUBLIC_KEY;
		/*Copies the computed checksum into the buffer*/
			System.arraycopy(hash.hash(pubKeyBytes, HashModes.CREATE_HASH), 0,
						messageBytes, Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
		/*Writes the Public key into the message*/
			System.arraycopy(pubKeyBytes, 0, messageBytes, Constants.MESSAGE_OFFSET, pubKeyBytes.length);
		/*Writes the buffer into the outputstream*/
			out.println(new String(Base64.getEncoder().encode(messageBytes),StandardCharsets.UTF_8));
	}
	/*Receives a Public Key extracts validates and returns it*/
	byte[] recievePubKey() throws ConnectionError {
		/*Wait for the incoming message*/
		while(!sc.hasNextLine());
		/*Decode the Base64 message*/
		byte[] messageBytes = Base64.getDecoder().decode(sc.nextLine().getBytes(StandardCharsets.UTF_8));
		/*Validate the Header*/
		Panic.R_UNLESS(messageBytes[Constants.HEADER_OFFSET] == ChatMagicNumbers.PUBLIC_KEY,messageBytes,this);
		/*Validate the Checksum over the Public Key*/
		hash.hash(messageBytes, HashModes.VALIDATE_HASH);
		/*Extract the Public Key*/
		return Arrays.copyOfRange(messageBytes, Constants.MESSAGE_OFFSET, messageBytes.length);
	}
	byte[] setUpCrypto(SecretKeySpec AES_Key) throws InvalidKeyException, IOException {
		try {
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, AES_Key);
			//cipher.doFinal(Constants.TEST_STRING.getBytes(StandardCharsets.UTF_8));
			return cipher.getParameters().getEncoded();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			/*This should never happen*/
			throw new AssertionError();
		}
	}
	void checkEncryption(boolean opened) {
		if(opened) {
			byte[] toEnc = new byte[Constants.HEADER_SIZE+Constants.CHECKSUM_SIZE+Constants.TEST_BYTES.length];
			System.arraycopy(hash.hash(Constants.TEST_BYTES, HashModes.CREATE_HASH), 0, toEnc, Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
			System.arraycopy(Constants.TEST_BYTES, 0, toEnc, Constants.MESSAGE_OFFSET, Constants.TEST_BYTES.length);
			System.out.println("0"+Arrays.toString(toEnc));
			byte[] encMessage = crypto.cryptoOperation(toEnc, CryptoModes.ENCRYPT);
			byte[] message = new byte[Constants.HEADER_SIZE+encMessage.length];
			message[Constants.HEADER_OFFSET] = ChatMagicNumbers.ENC_TEST_STRING;
			System.arraycopy(encMessage, 0, message, Constants.CHECKSUM_OFFSET, encMessage.length);
			out.println(new String(Base64.getEncoder().encode(message),StandardCharsets.UTF_8));
			byte recieved = extractHeader();
			if(recieved != ChatMagicNumbers.ENC_SUCCESS) {
				Log.log(new String[] {
						getIP_PORT()
				}, LogType.TEST_STING_DECRYPTION_FAILED);
				throw new ConnectionError(message, this);
			}
		} else {
			while(!sc.hasNextLine());
			byte[] message = Base64.getDecoder().decode(sc.nextLine().getBytes(StandardCharsets.UTF_8));
			System.out.println("a"+Arrays.toString(message));
			Panic.R_UNLESS(message[Constants.HEADER_OFFSET] == ChatMagicNumbers.ENC_TEST_STRING, message, this);
			byte[] decMessage = crypto.cryptoOperation(
					message,CryptoModes.DECRYPT);
			System.out.println("b"+Arrays.toString(decMessage));
			byte[] toHash = new byte[decMessage.length+Constants.HEADER_SIZE];
			Arrays.fill(toHash, (byte)(0));
			System.arraycopy(decMessage, 0, toHash, Constants.CHECKSUM_OFFSET, decMessage.length);
			System.out.println("c"+Arrays.toString(toHash));
			hash.hash(toHash, HashModes.VALIDATE_HASH);
			if(Arrays.equals(
					Arrays.copyOfRange(toHash, Constants.MESSAGE_OFFSET, toHash.length),
					Constants.TEST_BYTES)) {
				sendHeader(ChatMagicNumbers.ENC_SUCCESS);
			}else{
				sendHeader(ChatMagicNumbers.ENC_ERROR);
				Log.log(new String[] {
					getIP_PORT()
				} , LogType.TEST_STING_DECRYPTION_FAILED);
				throw new ConnectionError(message, this);
			}
		}
	}
	public Socket getSocket() {
		return this.socket;
	}
	/*Returns the name of the other ChatClient*/
	public String getOtherName() {
		return this.otherName;
	}
	@Override
	/*Returns a String containing the other ChatClients name with his ip address and port*/
	public String toString() {
		return "Connection: "+otherName+" to ip: "+
				socket.getInetAddress().getHostAddress()+":"+
				socket.getLocalPort();
	}
	public String getIP_PORT() {
		return socket.getInetAddress().getHostAddress()+"/"+socket.getPort();
	}
}	