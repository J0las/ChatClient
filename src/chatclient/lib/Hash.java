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

package chatclient.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import chatclient.Connection;

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
			case VALIDATE_HASH:
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
		case CREATE_HASH:
			/*Return the hash of the message*/
			return md.digest(message);
		}
		return null;
	}	
}