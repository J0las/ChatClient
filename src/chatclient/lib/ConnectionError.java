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
//import java.lang.Error;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

import chatclient.Connection;
import chatclient.log.Log;
import chatclient.log.LogType;
@SuppressWarnings("serial")
public class ConnectionError extends Error {

	public ConnectionError(byte[] messageBytes, Connection con) {
		/*Tries to close the socket*/
		try {
			con.getSocket().close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*Prints out connection name if defined and last message*/
		System.out.println("An Exception occured in Connection: "+
				((con.getOtherName().isEmpty())?"Undefinded Name":con.getOtherName())+
				"\n"+Arrays.toString(messageBytes)+"\n"+
				new String(messageBytes,StandardCharsets.UTF_8));
	}
	/*Case for invalid Hash*/
	public ConnectionError(byte[] calcHash, byte[] sendHash, byte[] messageContents, Connection con) {
		allways(con);
		/*Log the event*/
		Log.log(new String[] {
				con.getIP_PORT(),
				con.getOtherName(),
				new String(Base64.getEncoder().encode(calcHash), StandardCharsets.UTF_8),
				new String(Base64.getEncoder().encode(sendHash), StandardCharsets.UTF_8),
				new String(messageContents, StandardCharsets.UTF_8)
		}, LogType.HASH_INVALID);
	}
	public ConnectionError(byte sendHeader, byte expectedHeader, Connection con) {
		allways(con);
		/*Log the event*/
		Log.log(new String[] {
				con.getIP_PORT(),
				con.getOtherName(),
				ByteConverter.byteToHex(sendHeader),
				ByteConverter.byteToHex(expectedHeader)
		}, LogType.HEADER_INVALID);
	}
	private void allways(Connection con) {
		/*Tries to close the socket*/
		try {
			con.getSocket().close();
		} catch (IOException e) {
		}	
		/*Stop the thread through an interrupt*/
		con.interrupt();
	}
}	
