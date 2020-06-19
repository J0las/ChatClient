package chatclient.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import chatclient.Connection;
import chatclient.lib.HashModes;

public class Hash {
	public Hash(Connection con){
		try {
			this.md = MessageDigest.getInstance("SHA-256");
			this.con = con;
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
	}
	private MessageDigest md;
	private Connection con;
	/*Computes or validates a SHA-256 hash*/
	public synchronized byte[] hash(byte[] message , HashModes mode) throws ConnectionError{
		switch(mode) {
		/*Case for validating a given hash*/
			case validate_Hash:
			/*Extract the Checksum from the message*/
			byte[] 		checksum		= Arrays.copyOfRange(message, Constants.CHECKSUM_OFFSET,
											Constants.MESSAGE_OFFSET);
			/*Extract the Content of the message*/
			byte[] 		messageContent 	= Arrays.copyOfRange(message, Constants.MESSAGE_OFFSET,
											message.length);
			/*Compute the hash*/
			byte[] computedHash = md.digest(messageContent);
			/*Throws an Exception received and computed checksum do not match*/
			Panic.R_UNLESS(Arrays.equals(computedHash,checksum), message,con);
			break;
		case create_Hash:
			/*Return the hash of the message*/
			return md.digest(message);
		}
		return null;
	}	
}