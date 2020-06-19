/*
 *  Copyright (C) 2020  Felix Johannsmann, Johan Bücker
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
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
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import chatclient.lib.ChatMagicNumbers;
import chatclient.lib.ConnectionError;
import chatclient.lib.ConnectionErrorException;
import chatclient.lib.Constants;
import chatclient.lib.Crypto;
import chatclient.lib.CryptoModes;
import chatclient.lib.HashModes;
import chatclient.lib.Panic;
import chatclient.lib.Hash;

public class Connection {
	/*Socket for connection to the other ChatClient*/
	private Socket 			socket;
	/*printstream for easy output*/
	private PrintStream 	out;
	/*Name of the other ChatCLient*/
	private String 			name	= "";
	/*scanner for easy input*/
	private Scanner 		sc;
	/*Represents the state of the connection*/
	private boolean 		closed 	= false;
	/*Message digest for creating or validating hashes*/
	private Hash hash;
	private Crypto crypto;
	Connection(Socket socket,String ownName,boolean opendConnection) 
				throws ConnectionErrorException{
		this.hash = new Hash(this);
		this.socket=socket;
		try {
			/*Create a printstream for easy output*/
			this.out = new PrintStream(socket.getOutputStream(),true,StandardCharsets.UTF_8);
			/*Create an scanner for easy input*/
			sc = new Scanner(socket.getInputStream(),StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*Branch if the connection was opened from this ChatClient*/
		if(opendConnection){
			/*Sends the first header*/
			sendHeader(ChatMagicNumbers.HELLO);
			/*Checks if the response is valid*/
			checkHeader(ChatMagicNumbers.ACCEPTED_CONNECTION);
			/*Sends the own name to the other ChatClient*/
			sendName(ownName);
			/*Receives the name of the other ChatClient*/
			recieveName();
		/*Branch if the connection was opened from the other ChatClient*/
		}else {
			/*Checks if the first Message is valid*/
			checkHeader(ChatMagicNumbers.HELLO);
			/*Send the Accepted Connection Header*/
			sendHeader(ChatMagicNumbers.ACCEPTED_CONNECTION);
			/*Receives the name of the other ChatClient*/
			recieveName();
			/*Sends his own Name to the other ChatCllient*/
			sendName(ownName);
		}
	}
	/*Return true if there is a new Message*/
	boolean hasNewMessage(){
		return sc.hasNextLine();
	}
	/*Returns the content of the next Message*/
	String getNewMessage() throws ConnectionErrorException{
		/*Reads the base64 encoded message*/
		String message = sc.nextLine();
		/*Decodes the message into an bytearray*/
		byte[] messageBytes = Base64.getDecoder().decode(
								message.getBytes(StandardCharsets.UTF_8));
		/*Check if the header is as expected*/
		Panic.R_UNLESS(messageBytes[Constants.HEADER_OFFSET] == ChatMagicNumbers.ENC_MESSAGE,messageBytes,this);
		/*Validate the messageContents Checksum*/
		hash.hash(messageBytes,HashModes.validate_Hash);
		/*Extract the content of the message*/
		messageBytes=Arrays.copyOfRange(messageBytes, Constants.CHECKSUM_OFFSET + Constants.CHECKSUM_SIZE,
						messageBytes.length);
		/*Returns the String representation of the message contents*/
		return new String(messageBytes,StandardCharsets.UTF_8);
	}
	/*Send a message to the other ChatCLient*/
	void sendMessage(String message) {
		/*bytearray for the message + headerbyte*/
		byte[] messageBytes = new byte[message.length()+Constants.CHECKSUM_SIZE + Constants.CHECKSUM_SIZE];
		/*Sets the headerbyte to ENC_MESSAGE*/
		messageBytes[Constants.HEADER_OFFSET] = ChatMagicNumbers.ENC_MESSAGE;
		/*Computes the checksum over the messageContents and writes it into the buffer*/
		try {
			System.arraycopy(hash.hash(message.getBytes(StandardCharsets.UTF_8), HashModes.create_Hash), 0,
							messageBytes, Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
		} catch (ConnectionError e) {
			e.printStackTrace();
		}
		/*Appends the message contents to the header*/
		System.arraycopy(message.getBytes(StandardCharsets.UTF_8),
						 0, messageBytes,Constants.MESSAGE_OFFSET,
						 message.length());
		/*Sends the message as base64 encoded String*/
		out.println(new String(Base64.getEncoder().encode(messageBytes),
					StandardCharsets.UTF_8));
	}
	/*Sends the name of this ChatClient to the other ChatClient*/
	private void sendName(String ownName) throws ConnectionErrorException {
		/*bytearray for the name + headerbyte*/
		byte[] 	ownNameBytes 	= new byte[ownName.length()+Constants.HEADER_SIZE+Constants.CHECKSUM_SIZE];
		/*Sets the headerbyte to CONNECTION_NAME*/
				ownNameBytes[0] = ChatMagicNumbers.CONNECTION_NAME;
		/*Compute the CRC32 over the message and write it into the buffer*/
		System.arraycopy(hash.hash(ownName.getBytes(StandardCharsets.UTF_8),HashModes.create_Hash), 0,
				ownNameBytes,Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
		/*Appends the name contents to the header*/
		System.arraycopy(ownName.getBytes(StandardCharsets.UTF_8), 0,
						ownNameBytes,Constants.MESSAGE_OFFSET,
						ownName.length());
		/*Sends the name as base64 encoded String*/
		out.println(new String(Base64.getEncoder().encode(ownNameBytes),
					StandardCharsets.UTF_8));
	}
	/*Recieves the name of the other ChatClient and stores it in name*/
	private void recieveName() throws ConnectionErrorException{
		/*Waits for a new message*/
		while(!sc.hasNextLine());
		/*Reads the base64 encoded message*/
		String 	name = sc.nextLine();
		/*decodes the message*/
		byte[] 	nameBytes = Base64.getDecoder().decode(name.getBytes(StandardCharsets.UTF_8));
		/*Validates the CRC32 CHecksum over the name*/
			hash.hash(nameBytes, HashModes.validate_Hash);
		/*Checks if the headervalue is as expected*/ 
			Panic.R_UNLESS(nameBytes[0] == ChatMagicNumbers.CONNECTION_NAME,nameBytes,this);
		/*Copys the content of the message*/
				nameBytes = Arrays.copyOfRange(nameBytes, Constants.MESSAGE_OFFSET, nameBytes.length);
		/*Sets name to the String representation of the message*/
		this.name = new String(nameBytes,StandardCharsets.UTF_8);
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
		/*sets the closed flag to true*/
		closed = true;
	}
	/*Send the specified header*/
	private void sendHeader(byte header){
		out.println(new String(Base64.getEncoder().encode(new byte[] {header}),
								StandardCharsets.UTF_8));	
	}
	/*Check if the header matches the specified magic*/
	private void checkHeader(byte header) throws ConnectionErrorException{
		/*Wait for the response*/
		while(!sc.hasNextLine());
		/*Check if the response contains the expected value*/
		byte[] response = Base64.getDecoder().decode(sc.nextLine());
		Panic.R_UNLESS(response[Constants.HEADER_OFFSET] == header,response,this);
	}
	@Override
	/*Returns a String containing the other ChatClients name with his ip address and port*/
	public String toString() {
		return "Connection: "+name+" to ip: "+
				socket.getInetAddress().getHostAddress()+":"+
				socket.getLocalPort();
	}
	private void keyExchange(boolean opened) throws ConnectionErrorException {
		try {
			byte[] 	sharedSecret = new byte[0];
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
				SecretKeySpec 		AES_Key 		= new SecretKeySpec(sharedSecret, 0, Constants.AES_KEY_LENGTH, "AES");
				byte[] 				encodedAES_Params	= setUpCrypto(AES_Key);
									this.crypto = new Crypto(AES_Key, encodedAES_Params);
					sendEncodedParams(encodedAES_Params);
									
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
			}
		}catch(Exception e) {
			throw new ConnectionErrorException("AES KEYEXCHANGE FAILED!".getBytes(StandardCharsets.UTF_8),this);
		}
	}
	private void sendEncodedParams(byte[] encodedAES_Params) throws ConnectionErrorException{
		byte[] 	message		= new byte[Constants.HEADER_SIZE*Constants.CHECKSUM_SIZE+encodedAES_Params.length];
				message[Constants.HEADER_OFFSET]	
						= ChatMagicNumbers.ENCODED_PARAMS;
				System.arraycopy(hash.hash(message, HashModes.create_Hash), 0, message, Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
				System.arraycopy(encodedAES_Params, 0, message, Constants.MESSAGE_OFFSET, encodedAES_Params.length);
				out.println(new String(Base64.getEncoder().encode(message),StandardCharsets.UTF_8));
		
	}
	private byte[] recieveEncodedParams() throws ConnectionErrorException {
		while(!sc.hasNextLine());
		byte[] message = Base64.getDecoder().decode(sc.nextLine().getBytes(StandardCharsets.UTF_8));
		Panic.R_UNLESS(message[Constants.HEADER_OFFSET]==ChatMagicNumbers.ENCODED_PARAMS, message, this);
		hash.hash(message, HashModes.validate_Hash);
		return Arrays.copyOfRange(message, Constants.MESSAGE_OFFSET, message.length);
	}
	/*Sends the PublicKey of this ChatClient to the other ChatClient*/
	void sendPubKey(KeyPair keyPair) throws ConnectionErrorException {
		/*Gets the encoded PublicKey*/
		byte[] 	pubKeyBytes 	= keyPair.getPublic().getEncoded();
		/*Creates a buffer for the message*/
		byte[] 	messageBytes 	= new byte[pubKeyBytes.length+Constants.HEADER_SIZE+Constants.CHECKSUM_SIZE];
		/*Writes the headerbyte into the buffer*/
				messageBytes[Constants.HEADER_OFFSET] = ChatMagicNumbers.PUBLIC_KEY;
		/*Copies the computed checksum into the buffer*/
			System.arraycopy(hash.hash(pubKeyBytes, HashModes.create_Hash), 0,
						messageBytes, Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
		/*Writes the Public key into the message*/
			System.arraycopy(pubKeyBytes, 0, messageBytes, Constants.MESSAGE_OFFSET, pubKeyBytes.length);
		/*Writes the buffer into the outputstream*/
			out.println(Base64.getEncoder().encode(messageBytes));
	}
	/*Receives a Public Key extracts validates and returns it*/
	byte[] recievePubKey() throws ConnectionErrorException {
		/*Wait for the incoming message*/
		while(!sc.hasNextLine());
		/*Decode the Base64 message*/
		byte[] messageBytes = Base64.getDecoder().decode(sc.nextLine().getBytes(StandardCharsets.UTF_8));
		/*Validate the Header*/
		Panic.R_UNLESS(messageBytes[Constants.HEADER_OFFSET] == ChatMagicNumbers.PUBLIC_KEY,messageBytes,this);
		/*Validate the Checksum over the Public Key*/
		hash.hash(messageBytes, HashModes.validate_Hash);
		/*Extract the Public Key*/
		return Arrays.copyOfRange(messageBytes, Constants.MESSAGE_OFFSET, messageBytes.length);
	}

	byte[] setUpCrypto(SecretKeySpec AES_Key) throws InvalidKeyException, IOException {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, AES_Key);
			return cipher.getParameters().getEncoded();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			/*This should never happen*/
			throw new AssertionError();
		}
	}
	public Socket getSocket() {
		return this.socket;
	}
	/*Returns the name of the other ChatClient*/
	public String getName() {
		return this.name;
	}
}	