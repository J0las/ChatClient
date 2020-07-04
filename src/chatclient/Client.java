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

package chatclient;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import chatclient.lib.ConnectionError;
import chatclient.lib.Constants;
import chatclient.lib.ErrorType;
import chatclient.lib.UnreachableIPException;

class Client {
	private static Scanner sc 	= new Scanner(System.in);
	
	static void run() throws ConnectionError{
		try {
		newConnection();
		} catch (UnreachableIPException e) {
			return;
		}
		Connection[] c = Connections.toArray();;
				c[0].sendMessage("Lol");
		System.out.println(Arrays.toString(c));
		printNewMessages(c[1]);
		c[1].sendMessage("Test1");
		c[1].sendMessage("Test2");
		c[0].sendMessage("Go");
		printNewMessages(c[0]);
		printNewMessages(c[1]);
		printNewMessages(c[0]);
		Runtime.getRuntime().exit(0);
	}
	static private void printNewMessages(Connection con) throws ConnectionError {
		if(con.isClosed()) Connections.remove(con);
		con.getNewMessages();
	}
	static void newConnection() throws ConnectionError, UnreachableIPException{
		/*Buffer for the raw ip*/
		byte[] ipBytes = new byte[4];
		{	
			/*Gets the ip in the format xxx.xxx.xxx.xxx*/
			System.out.println("Input ip:");
			String 		ip 	= sc.nextLine();
						ip 	= ip.replaceAll("\\s","");
			String[] 	ips = ip.split("\\.");
			for(int i=0;i<4;i++) {
				ipBytes[i] = (byte)(Integer.parseInt(ips[i]));
			}
		}
		/*Returns a new Connection*/
		Connection con = null;
			try {
				InetAddress ip = InetAddress.getByAddress(ipBytes);
				if(!ip.isReachable(1000)) throw new UnreachableIPException(ip);
				con = new Connection(
						new Socket(ip,
								Constants.STANDARD_PORT), true);
						con.start();
				Connections.add(con);
			} catch (IOException e) {
				throw new ConnectionError(e, con);
			}
	}
}
