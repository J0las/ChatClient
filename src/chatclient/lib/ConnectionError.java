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

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import chatclient.Connection;
import chatclient.Connections;
import chatclient.log.Log;
import chatclient.log.LogType;

@SuppressWarnings("serial")
public class ConnectionError extends Error {
	
	public ConnectionError(Connection con, ErrorType errorType) {
		always(con);
		switch(errorType) {
		case PUB_KEY_INVALID:
			Log.log(new String[] {
					con.getIP_PORT()
				}, LogType.PUB_KEY_INVALID);
			break;
		case TEST_STRING_DEC_FAILED:
			Log.log(new String[] {
					con.getIP_PORT()
				}, LogType.TEST_STING_DECRYPTION_FAILED);
		default:
			throw new IllegalArgumentException();
		}
	}
	public ConnectionError(IOException e, Connection con) {
	    always(con);
	    Log.log(new String[] {
                con.getIP_PORT(),
                e.getMessage()
            }, LogType.GENERAL_IO_ERROR);
	}
	/*Case for invalid Hash*/
	public ConnectionError(byte[] calcHash, byte[] sendHash, byte[] messageContents, Connection con) {
		always(con);
		/*Log the event*/
		Log.log(new String[] {
				con.getIP_PORT(),
				con.getOtherName(),
				ByteConverter.byteArrayToHexString(calcHash),
				ByteConverter.byteArrayToHexString(sendHash),
				new String(messageContents, StandardCharsets.UTF_8)
			}, LogType.HASH_INVALID);
	}
	/*Case for an invalid header*/
	public ConnectionError(byte sendHeader, byte expectedHeader, Connection con) {
		always(con);
		/*Log the event*/
		Log.log(new String[] {
				con.getIP_PORT(),
				con.getOtherName(),
				ByteConverter.byteToHex(sendHeader),
				ByteConverter.byteToHex(expectedHeader)
			}, LogType.HEADER_INVALID);
	}
	/*Case for an invalid message length*/
	public ConnectionError(byte[] rawMessage, Connection con) {
		always(con);
			Log.log(new String[] {
					con.getIP_PORT(),
					ByteConverter.byteArrayToHexString(rawMessage)
				}, LogType.MESSAGE_FORMAT_INVALID);
	}
	/*Case for an invalid base64 encoding of an recieved message*/
	public ConnectionError(String rawMessage, Connection con) {
		always(con);
		Log.log(new String[] {
				con.getIP_PORT(),
				rawMessage
		}, LogType.BASE64_ENCODING_INVALID);
	}
	private void always(Connection con) {
		con.closeConnection();
		/*Stop the thread through an interrupt*/
		con.interrupt();
		/*Remove this connection from the list of available connections*/
		Connections.remove(con);
	}
}	
