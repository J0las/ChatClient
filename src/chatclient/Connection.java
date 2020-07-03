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

/*
 * 	Connections represent a connection to another ChatClient and
 * 	allow for the sending and receiving of messages 
 * 	after the object has been successfully initialized.
 * 
 * 	Each connection has its own thread retrieving new messages from the
 * 	inputstream as a request to it blocks the current thread 
 * 	and would halt execution of the program.
 * 
 * 	When the connection is closed the thread is stopped and releases all
 * 	resources like crypto or hash objects 
 * 	while also closing the socket and its io streams
 *  
 */

package chatclient;

import java.awt.Color;
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
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.JTextPane;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import chatclient.lib.ByteConverter;
import chatclient.lib.ChatMagicNumbers;
import chatclient.lib.ConnectionError;
import chatclient.lib.Constants;
import chatclient.lib.Crypto;
import chatclient.lib.CryptoModes;
import chatclient.lib.ErrorType;
import chatclient.lib.Hash;
import chatclient.lib.HashModes;
import chatclient.lib.QueueModes;
import chatclient.log.Log;
import chatclient.log.LogType;

public class Connection extends Thread {
    /* Socket for connection to the other ChatClient */
    private Socket socket;
    /* printstream for easy output */
    private PrintStream out;
    /* Name of the other ChatClient */
    private String otherName = "";
    /* Name of this ChatClient */
    private String ownName = "";
    /* scanner for easy input */
    private Scanner sc;
    /* Represents the state of the connection */
    private boolean closed = false;
    /* Hashobject for creating or validating hashes */
    private Hash hash;
    /* Cryptoobject for encrypting and decrypting messages and hashes */
    private Crypto crypto;
    /*
     * Queue for exchanging messages between the accepting thread and the message
     * handler
     */
    private Queue<String> input;
    private JTextPane pane;
    SimpleAttributeSet ownNameColor;
    SimpleAttributeSet otherNameColor;
    SimpleAttributeSet MessageColor;
    StyledDocument doc;

    Connection(Socket socket, String ownName, boolean openedConnection) throws ConnectionError {
        /* Setup objects */
        this.hash = new Hash(this);
        this.socket = socket;
        this.input = new LinkedList<String>();
        this.ownName = ownName;

        this.pane = new JTextPane();
        this.pane.setEditable(false);
        this.doc = this.pane.getStyledDocument();
        this.MessageColor = new SimpleAttributeSet();
        StyleConstants.setForeground(this.MessageColor, Color.BLACK);
        this.ownNameColor = new SimpleAttributeSet();
        StyleConstants.setForeground(this.ownNameColor, Color.BLUE);
        this.otherNameColor = new SimpleAttributeSet();
        StyleConstants.setForeground(this.otherNameColor, Color.GREEN);
        /* Logs that that a new connection was created */
        if (openedConnection) {
            Log.log(new String[] { getIP_PORT() }, LogType.CREATED_NEW_CONNECTION);
            /* Logs that a new connection was recieved */
        } else {
            Log.log(new String[] { getIP_PORT() }, LogType.INCOMMING_CONNECTION);
        }
        try {
            /* Create a printstream for easy output */
            this.out = new PrintStream(socket.getOutputStream(), true, StandardCharsets.UTF_8);
            /* Create an scanner for easy input */
            sc = new Scanner(socket.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ConnectionError(this, ErrorType.GENERAL_IO_ERROR);
        }
        /* Branch if the connection was opened from this ChatClient */
        if (openedConnection) {
            /* Sends the first header */
            sendHeader(ChatMagicNumbers.HELLO);
            /* Checks if the response is valid */
            checkHeader(ChatMagicNumbers.ACCEPTED_CONNECTION);
            /* Branch if the connection was opened from the other ChatClient */
        } else {
            /* Checks if the first Message is valid */
            checkHeader(ChatMagicNumbers.HELLO);
            /* Send the Accepted Connection Header */
            sendHeader(ChatMagicNumbers.ACCEPTED_CONNECTION);
            /* Receives the name of the other ChatClient */
        }
        /*
         * Exchange a AES key with the other ChatClient using the diffie-hellman
         * algorithm
         */
        keyExchange(openedConnection);
        /* Checks if the calculated AES key is valid and the same for both parties */
        checkEncryption(openedConnection);
        /* Branch if the connection was opened from this ChatClient */
        if (openedConnection) {
            /* Sends the own name to the other ChatClient */
            sendName();
            /* Receives the name of the other ChatClient */
            recieveName();
            /* Branch if the connection was opened from the other ChatClient */
        } else {
            /* Receives the name of the other ChatClient */
            recieveName();
            /* Sends his own Name to the other ChatCllient */
            sendName();
        }
        /* Logs that the connection was created successfully */
        Log.log(new String[] { getIP_PORT(), otherName }, LogType.CONNECTION_ESTABLISHED);
    }

    /************************************************/
    /*	                                            */
    /* Functions for get new messages:              */
    /* 	                                       		*/
    /************************************************/

    /* Thread function to receive incoming messages */
    @Override
    public void run() {
        /*
         * These exceptions are thrown when the socket is closed or the thread is
         * interrupted This way the Thread exits when the connection is closed
         */
        try {
            /* Checks if the thread is interrupted if not continues to loop */
            while (!this.isInterrupted()) {
                /*
                 * The scanner blocks until a new input is detected or the socket is closed and
                 * then returns a new line which is added to the end of the queue
                 */
                queue(sc.nextLine(), QueueModes.ADD);
            }
        } catch (NoSuchElementException | IllegalStateException e) {
            return;
        }
    }

    /*
     * Function to allow a thread safe handling of the queue making it possible to
     * add and remove strings from it
     */
    private synchronized String queue(String message, QueueModes mode) throws NoSuchElementException {
        /* Selects the mode of operation */
        switch (mode) {
        /* Case for adding a new string to the tail of the queue */
        case ADD:
            input.add(message);
            return null;
        /* Case for retrieving a string from the head of the queue */
        case GET:
            return input.remove();
        /* Handles illegal enum types */
        default:
            throw new IllegalArgumentException();
        }
    }

    /************************************************/
    /*	                                            */
    /* Functions for primitive io:                  */
    /* 	                                       		*/
    /************************************************/

    /* Send the specified header */
    private void sendHeader(byte header) {
        out.println(new String(Base64.getEncoder().encode(new byte[] { header }), StandardCharsets.UTF_8));
    }

    /* Check if the header matches the specified magic */
    private void checkHeader(byte header) throws ConnectionError {
        /* Wait for the response */
        while (!sc.hasNextLine())
            ;
        /* Check if the response contains the expected value */
        byte[] response = decodeBase64(sc.nextLine());
        if (response[Constants.HEADER_OFFSET] != header)
            throw new ConnectionError(response[Constants.HEADER_OFFSET], header, this);
    }

    /***************************************************************************/
    /*	                                                                       */
    /* Functions to exchange AES keys using the diffie hellman algorithm       */
    /* 	                                       		                           */
    /***************************************************************************/

    /* Exchanges keys with the other ChatClient */
    private void keyExchange(boolean opened) throws ConnectionError {
        /* If an exception is thrown the process is aborted */
        try {
            /*
             * This buffer holds the then calculated shared secret that both ChatClients
             * possess
             */
            byte[] sharedSecret = new byte[0];
            /*
             * This is the AES key both ChatClients will use for encryption and decryption
             * of their messaages
             */
            SecretKeySpec AES_Key;
            /* Branch if the connection was opened from this ChatClient */
            if (opened) {
                /* Prepare the KeyPairGenerator for the diffie hellman algorithm */
                KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
                /* Set the configured RSA key length */
                keyGen.initialize(Constants.DH_KEY_SIZE);
                /* Generate the keypair */
                KeyPair keyPair = keyGen.generateKeyPair();
                /* Create a KeyAgreement object to calculate the shared secret using the diffie hellman algorithm*/
                KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                /*Initiate the KeyAgreement object with the private key of this ChatClient*/
                keyAgree.init(keyPair.getPrivate());
                /*Send the public key of this ChatClient to the other ChatClient*/
                sendPubKey(keyPair);
                /*Create a KeyFactory object to unpack the other ChatClients public key*/
                KeyFactory keyFactory = KeyFactory.getInstance("DH");
                /*Store the recieved public key*/
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(recievePubKey());
                /*unpack the public key of the other ChatClient*/
                PublicKey otherPubKey = keyFactory.generatePublic(x509KeySpec);
                /*add the other public key to the KeyAgreement object*/
                keyAgree.doPhase(otherPubKey, true);
                /*calculate the shared secret*/
                sharedSecret = keyAgree.generateSecret();
                /*Generate the AES key with the defined length*/
                AES_Key = new SecretKeySpec(sharedSecret, 0, Constants.AES_KEY_LENGTH, "AES");
                /*Store the AES parameters including the iv to allow the other ChatClient to encrypt or decrypt the messages*/
                byte[] encodedAES_Params = setUpCrypto(AES_Key);
                /*Send the AES parameters to the other ChatClient*/
                sendEncodedParams(encodedAES_Params);
                /*Set up the crypto object with the shared AES key and parameters*/
                this.crypto = new Crypto(AES_Key, encodedAES_Params);
                /* Branch if the connection was opened from the other ChatClient */
            } else {
                /*Create a KeyFactory object to unpack the other ChatClients public key*/
                KeyFactory keyFactory = KeyFactory.getInstance("DH");
                /*Store the received public key*/
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(recievePubKey());
                /*unpack the public key of the other ChatClient*/
                PublicKey otherPubKey = keyFactory.generatePublic(x509KeySpec);
                /*Extract the diffie hellman algorithm parameters from the other ChatClients public key*/
                DHParameterSpec otherDHParams = ((DHPublicKey) otherPubKey).getParams();
                /*Prepare the KeyPairGenerator for the diffie hellman algorithm */
                KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
                /*Initialize the KeyPairGenerator object with the extracted parameters*/
                keyPairGen.initialize(otherDHParams);
                /* Generate the keypair */
                KeyPair keyPair = keyPairGen.generateKeyPair();
                /* Create a KeyAgreement object to calculate the shared secret using the diffie hellman algorithm*/
                KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
                /*Initiate the KeyAgreement object with the private key of this ChatClient*/
                keyAgree.init(keyPair.getPrivate());
                /*Send the public key of this ChatClient to the other ChatClient*/
                sendPubKey(keyPair);
                /*add the other public key to the KeyAgreement object*/
                keyAgree.doPhase(otherPubKey, true);
                /*calculate the shared secret*/
                sharedSecret = keyAgree.generateSecret();
                /*Generate the AES key with the defined length*/
                AES_Key = new SecretKeySpec(sharedSecret, 0, Constants.AES_KEY_LENGTH, "AES");
                /*Set up the crypto object with the shared AES key and the received parameters*/
                this.crypto = new Crypto(AES_Key, recieveEncodedParams());
            }
            /*Log the SHA256 of the AES key*/
            Log.log(new String[] { getIP_PORT(),
                    new String(ByteConverter
                            .byteArrayToHexString(hash.hash(AES_Key.getEncoded(), HashModes.CREATE_HASH))) },
                    LogType.AES_KEY_HASH);
        } catch (InvalidKeySpecException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            /* Catch errors that might occur if invalid data was send by the other ChatClient*/
            throw new ConnectionError(this, ErrorType.PUB_KEY_INVALID);
        } catch (NoSuchAlgorithmException e) {
            /*This should never happen*/
            throw new AssertionError();
        }
    }

    /* Receives a Public Key extracts validates and returns it */
    byte[] recievePubKey() throws ConnectionError {
        /* Wait for the incoming message */
        while (!sc.hasNextLine());
        /* Decode the Base64 message */
        byte[] rawMessage = decodeBase64(sc.nextLine());
        checkMessageFormat(rawMessage);
        /* Validate the header */
        if (rawMessage[Constants.HEADER_OFFSET] != ChatMagicNumbers.PUBLIC_KEY)
            throw new ConnectionError(rawMessage[Constants.HEADER_OFFSET], ChatMagicNumbers.PUBLIC_KEY, this);
        /* Validate the Checksum over the Public Key */
        hash.hash(rawMessage, HashModes.VALIDATE_HASH);
        /* Extract the public key */
        return Arrays.copyOfRange(rawMessage, Constants.MESSAGE_OFFSET, rawMessage.length);
    }

    /* Sends the PublicKey of this ChatClient to the other ChatClient */
    private void sendPubKey(KeyPair keyPair) throws ConnectionError {
        /* Gets the encoded PublicKey */
        byte[] pubKeyBytes = keyPair.getPublic().getEncoded();
        /* Creates a buffer for the message */
        byte[] messageBytes = new byte[pubKeyBytes.length + Constants.HEADER_SIZE + Constants.CHECKSUM_SIZE];
        /* Writes the headerbyte into the buffer */
        messageBytes[Constants.HEADER_OFFSET] = ChatMagicNumbers.PUBLIC_KEY;
        /* Copies the computed checksum into the buffer */
        System.arraycopy(hash.hash(pubKeyBytes, HashModes.CREATE_HASH), 0, messageBytes, Constants.CHECKSUM_OFFSET,
                Constants.CHECKSUM_SIZE);
        /* Writes the Public key into the message */
        System.arraycopy(pubKeyBytes, 0, messageBytes, Constants.MESSAGE_OFFSET, pubKeyBytes.length);
        /* Writes the buffer into the outputstream */
        out.println(new String(Base64.getEncoder().encode(messageBytes), StandardCharsets.UTF_8));
    }
    
    /*Return the AES parameters needed to encrypt or decrypt data*/
    byte[] setUpCrypto(SecretKeySpec AES_Key) throws InvalidKeyException{
        try {
            /*Get a Cipher for AES in cipher-block-chaining mode with PKCS5 padding*/
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            /*Initialize the cipher in encryption mode with the exchanged AES key*/
            cipher.init(Cipher.ENCRYPT_MODE, AES_Key);
            /*Extract the parameters and return them*/
            return cipher.getParameters().getEncoded();
        } catch (NoSuchAlgorithmException | NoSuchPaddingException |  IOException  e) {
            /* This should never happen */
            throw new AssertionError();
        }
    }

    /*Sends the encoded AES parameters needed for crypto operations to the other ChatClient*/
    private void sendEncodedParams(byte[] encodedAES_Params) throws ConnectionError {
        /*Allocate a buffer for the whole message*/
        byte[] message = new byte[Constants.HEADER_SIZE + Constants.CHECKSUM_SIZE + encodedAES_Params.length];
        /*Set the header to the correct value*/
        message[Constants.HEADER_OFFSET] = ChatMagicNumbers.ENCODED_PARAMS;
        /*Copy the SHA256 over the encoded parameters into the buffer*/
        System.arraycopy(hash.hash(encodedAES_Params, HashModes.CREATE_HASH), 0, message, Constants.CHECKSUM_OFFSET,
                Constants.CHECKSUM_SIZE);
        /*Copy the encoded parameters into the buffer*/
        System.arraycopy(encodedAES_Params, 0, message, Constants.MESSAGE_OFFSET, encodedAES_Params.length);
        /*Encode the buffer with BASE64 and send it*/
        out.println(new String(Base64.getEncoder().encode(message), StandardCharsets.UTF_8));
    }
    
    /*Recieve the encoded AES parameters needed for crypto operations from the other ChatClient*/
    private byte[] recieveEncodedParams() throws ConnectionError {
        /*Wait for the message to arrive*/
        while (!sc.hasNextLine());
        /*Decode the BASE64 massage to raw bytes*/
        byte[] rawMessage = decodeBase64(sc.nextLine());
        checkMessageFormat(rawMessage);
        /*Check if the header byte is as expected*/
        if (rawMessage[Constants.HEADER_OFFSET] != ChatMagicNumbers.ENCODED_PARAMS)
            throw new ConnectionError(rawMessage[Constants.HEADER_OFFSET], ChatMagicNumbers.ENCODED_PARAMS, this);
        /*Verify the hash over the parameters*/
        hash.hash(rawMessage, HashModes.VALIDATE_HASH);
        /*Return the extracted parameters*/
        return Arrays.copyOfRange(rawMessage, Constants.MESSAGE_OFFSET, rawMessage.length);
    }

    /************************************************/
    /*	                                            */
    /* Functions to verify the exchanged AES key    */
    /* 	                                       		*/
    /************************************************/

    /*Checks if the exchanged AES key is the same for both ChatClients*/
    void checkEncryption(boolean opened) throws ConnectionError{
        /* Branch if the connection was opened from this ChatClient */
        if (opened) {
            /*Allocate a buffer for the parts of the message to be encrypted*/
            byte[] toEnc = new byte[Constants.HEADER_SIZE + Constants.CHECKSUM_SIZE + Constants.TEST_BYTES.length];
            /*Copy the SHA256 over a static shared sequence of bytes into the buffer*/
            System.arraycopy(hash.hash(Constants.TEST_BYTES, HashModes.CREATE_HASH), 0, toEnc,
                    Constants.CHECKSUM_OFFSET, Constants.CHECKSUM_SIZE);
            /*Copy the static shared sequence of bytes into the buffer*/
            System.arraycopy(Constants.TEST_BYTES, 0, toEnc, Constants.MESSAGE_OFFSET, Constants.TEST_BYTES.length);
            byte[] encMessage = crypto.cryptoOperation(toEnc, CryptoModes.ENCRYPT);
            /*Allocate a new buffer for the whole encrypted message to account for padding*/
            byte[] message = new byte[Constants.HEADER_SIZE + encMessage.length];
            /*Set the header byte to the correct value*/
            message[Constants.HEADER_OFFSET] = ChatMagicNumbers.ENC_TEST_STRING;
            /*Copy the encrypted message into the new buffer*/
            System.arraycopy(encMessage, 0, message, Constants.CHECKSUM_OFFSET, encMessage.length);
            /*Send the buffer as a BASE64 encoded string to the other ChatClient*/
            out.println(new String(Base64.getEncoder().encode(message), StandardCharsets.UTF_8));
            /*Check the response header*/
            checkHeader(ChatMagicNumbers.ENC_SUCCESS);
            /*If the previous check succeeded signal the other ChatClient to switch to encrypted communication*/
            sendHeader(ChatMagicNumbers.SWITCH_TO_ENC_MODE);
        /* Branch if the connection was opened from this ChatClient */    
        } else {
            /*Wait for the message to arrive*/
            while (!sc.hasNextLine());
            /*Decode the BASE64 massage to raw bytes*/
            byte[] rawMessage = decodeBase64(sc.nextLine());
            /*Check if the header byte is as expected*/
            if (rawMessage[Constants.HEADER_OFFSET] != ChatMagicNumbers.ENC_TEST_STRING)
                throw new ConnectionError(rawMessage[Constants.HEADER_OFFSET], ChatMagicNumbers.ENC_TEST_STRING, this);
            /*Decrypt the message*/
            byte[] decMessage = crypto.cryptoOperation(rawMessage, CryptoModes.DECRYPT);
            /*Allocate a buffer to store the decrypted messae in the expected format*/
            byte[] toHash = new byte[decMessage.length + Constants.HEADER_SIZE];
            /*COpy the decrypted message into the new buffer*/
            System.arraycopy(decMessage, 0, toHash, Constants.CHECKSUM_OFFSET, decMessage.length);
            hash.hash(toHash, HashModes.VALIDATE_HASH);
            if (Arrays.equals(Arrays.copyOfRange(toHash, Constants.MESSAGE_OFFSET, toHash.length),
                    Constants.TEST_BYTES)) {
                sendHeader(ChatMagicNumbers.ENC_SUCCESS);
                checkHeader(ChatMagicNumbers.SWITCH_TO_ENC_MODE);
            } else {
                sendHeader(ChatMagicNumbers.ENC_ERROR);
                throw new ConnectionError(this, ErrorType.TEST_STRING_DEC_FAILED);
            }
        }
    }

    /************************************************/
    /*	                                            */
    /* Functions to exchange encrypted names:       */
    /* 	                                       		*/
    /************************************************/

    /* Sends the name of this ChatClient to the other ChatClient */
    private void sendName() throws ConnectionError {
        byte[] nameBytes = ownName.getBytes(StandardCharsets.UTF_8);
        /* Allocate a buffer for the hash and name to be encrypted */
        byte[] toEnc = new byte[Constants.HEADER_SIZE + Constants.CHECKSUM_SIZE + nameBytes.length];
        /* Copy the hash into the buffer */
        System.arraycopy(hash.hash(nameBytes, HashModes.CREATE_HASH), 0, toEnc, Constants.CHECKSUM_OFFSET,
                Constants.CHECKSUM_SIZE);
        /* Copy the name in UTF-8 encoding into the buffer */
        System.arraycopy(nameBytes, 0, toEnc, Constants.MESSAGE_OFFSET, nameBytes.length);
        /* Encrypt the buffer except for the first placeholder byte */
        byte[] enc = crypto.cryptoOperation(toEnc, CryptoModes.ENCRYPT);
        /* Allocate a buffer for the complete message */
        byte[] message = new byte[Constants.HEADER_SIZE + enc.length];
        /* Setting the headerbyte */
        message[Constants.HEADER_OFFSET] = ChatMagicNumbers.CONNECTION_NAME;
        /* Copy the encrypted hash and name into the buffer */
        System.arraycopy(enc, 0, message, Constants.CHECKSUM_OFFSET, enc.length);
        /* Send the name as a Base64 encoded String */
        out.println(new String(Base64.getEncoder().encode(message), StandardCharsets.UTF_8));
    }

    /* Recieves the name of the other ChatClient and stores it in otherName */
    private void recieveName() throws ConnectionError {
        /* Wait for the next message */
        while (!sc.hasNextLine());
        /* Decode the incoming message from Base64 to raw bytes */
        byte[] rawMessage = decodeBase64(sc.nextLine());
        checkMessageFormat(rawMessage);
        /* Check if the headerbyte is valid */
        if (rawMessage[Constants.HEADER_OFFSET] != ChatMagicNumbers.CONNECTION_NAME)
            throw new ConnectionError(rawMessage[Constants.HEADER_OFFSET], ChatMagicNumbers.CONNECTION_NAME, this);
        /* Decrypt the Message */
        byte[] dec = crypto.cryptoOperation(rawMessage, CryptoModes.DECRYPT);
        /* Allocate a new buffer to retain message format */
        byte[] toHash = new byte[Constants.HEADER_SIZE + dec.length];
        /* Store the decrypted hash and message into the new buffer */
        System.arraycopy(dec, 0, toHash, Constants.CHECKSUM_OFFSET, dec.length);
        /* Validate the hash */
        hash.hash(toHash, HashModes.VALIDATE_HASH);
        /* Store the name of the other chatclient */
        this.otherName = new String(Arrays.copyOfRange(toHash, Constants.MESSAGE_OFFSET, toHash.length),
                StandardCharsets.UTF_8);
    }

    /*********************************************/
    /*                                           */
    /* Functions to send / receive messages      */
    /*                                           */
    /*********************************************/

    /* Send a message to the other ChatCLient */
    void sendMessage(String message) {
        /* Allocates a buffer for the UTF-8 representation of the message */
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        /* Calculates the hash and stores it inside a new buffer */
        byte[] sha = hash.hash(messageBytes, HashModes.CREATE_HASH);
        /* Allocates a buffer the hash and message for encryption */
        byte[] shaMessage = new byte[Constants.HEADER_SIZE + Constants.CHECKSUM_SIZE + messageBytes.length];
        /* Copies the hash into the buffer */
        System.arraycopy(sha, 0, shaMessage, Constants.CHECKSUM_OFFSET, sha.length);
        /* Copies the message into the buffer */
        System.arraycopy(messageBytes, 0, shaMessage, Constants.MESSAGE_OFFSET, messageBytes.length);
        /* Encrypts the hash and message */
        shaMessage = crypto.cryptoOperation(shaMessage, CryptoModes.ENCRYPT);
        /* Allocates a new buffer to retain message format */
        byte[] completeMessage = new byte[Constants.HEADER_SIZE + shaMessage.length];
        /* Sets the headerbyte */
        completeMessage[Constants.HEADER_OFFSET] = ChatMagicNumbers.ENC_MESSAGE;
        /* Copies the encrypted hash and message into the buffer */
        System.arraycopy(shaMessage, 0, completeMessage, Constants.CHECKSUM_OFFSET, shaMessage.length);
        /* sends out the crafted message as a Base64 encoded String */
        out.println(new String(Base64.getEncoder().encode(completeMessage), StandardCharsets.UTF_8));
        /* Logs the message */
        Log.log(new String[] { getIP_PORT(), this.otherName, message }, LogType.MESSAGE_SEND);
    }

    /* Returns the content of the next Message */
    private String getNewMessage(String stringMessage) throws ConnectionError {
        /* Decode the message from Base64 to raw bytes */
        byte[] rawMessage = decodeBase64(stringMessage);
        /* Checks if the message conforms to the format of expected messages */
        checkMessageFormat(rawMessage);
        /* Validates the header */
        if (rawMessage[Constants.HEADER_OFFSET] != ChatMagicNumbers.ENC_MESSAGE)
            throw new ConnectionError(rawMessage[Constants.HEADER_OFFSET], ChatMagicNumbers.ENC_MESSAGE, this);
        /* Decrypts the data */
        byte[] decData = crypto.cryptoOperation(rawMessage, CryptoModes.DECRYPT);
        /* Allocates a new buffer to retain message format */
        byte[] forHash = new byte[decData.length + Constants.HEADER_SIZE];
        /* Copies the decrypted hash and message into the new buffer */
        System.arraycopy(decData, 0, forHash, Constants.CHECKSUM_OFFSET, decData.length);
        /* Validates the hash */
        hash.hash(forHash, HashModes.VALIDATE_HASH);
        /* Extracts the message */
        String message = new String(Arrays.copyOfRange(forHash, Constants.MESSAGE_OFFSET, forHash.length),
                StandardCharsets.UTF_8);
        /* Logs the new message */
        Log.log(new String[] { getIP_PORT(), this.otherName, message }, LogType.MESSAGE_RECIEVED);
        /* Returns the message */
        return message;
    }

    /*********************************************/
    /*                                           */
    /* Functions to verify / decode messages     */
    /*                                           */
    /*********************************************/

    private void checkMessageFormat(byte[] rawMessage) throws ConnectionError {
        if (rawMessage.length <= Constants.HEADER_SIZE + Constants.CHECKSUM_SIZE)
            throw new ConnectionError(rawMessage, this);
    }

    private byte[] decodeBase64(String message) {
        try {
            return Base64.getDecoder().decode(message.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new ConnectionError(message, this);
        }
    }

    /*********************************************/
    /*                                           */
    /* Functions to close this connection        */
    /*                                           */
    /*********************************************/

    public void abortSetup() {
        sendHeader(ChatMagicNumbers.CLOSE_CONNECTION);
        sc.close();
        closeConnection();
    }

    /* Closes the socket */
    private void closeConnection() {
        /* Tries to close the socket */
        try {
            socket.close();
        } catch (IOException e) {
        }
        /* Logs the closing of this connection */
        Log.log(new String[] { socket.getInetAddress().getHostAddress() + " / " + socket.getPort() },
                LogType.CONNECTION_CLOSED);
        /* sets the closed flag to true */
        closed = true;
    }

    /***************/
    /*             */
    /* Getters     */
    /*             */
    /***************/

    public Socket getSocket() {
        return this.socket;
    }

    /* Returns the name of the other ChatClient */
    public String getOtherName() {
        return this.otherName;
    }

    /* Returns true if the connection was closed */
    boolean isClosed() {
        /* if it was already closed return true */
        if (closed)
            return true;
        /* if the socket is closed set closed to true */
        if (socket.isClosed())
            closed = true;
        /* Return the value of closed */
        return closed;
    }

    public String getIP_PORT() {
        return socket.getInetAddress().getHostAddress() + "/" + socket.getPort();
    }

    /*
     * Returns a string containing the other ChatClients name with his ip address
     * and port
     */
    @Override
    public String toString() {
        return "Connection: " + otherName + " to ip: " + socket.getInetAddress().getHostAddress() + ":"
                + socket.getLocalPort();
    }
    /* Tmp */

    void getNewMessages() {
        try {
            String message = queue(null, QueueModes.GET);
            while (message != null) {
                System.out.println(getNewMessage(message));
                message = queue(null, QueueModes.GET);
            }
        } catch (NoSuchElementException e) {
            return;
        }
    }
}