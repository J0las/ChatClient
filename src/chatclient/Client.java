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

import chatclient.lib.ArrayModifications;
import chatclient.lib.ConnectionError;
import chatclient.lib.Constants;

class Client {
	private Scanner sc 	= new Scanner(System.in);
	private String 	name;
	Client(String name){
		this.name = name;
	}
	void run() throws ConnectionError{
		newConnection();
		Connection[] c = Connections.modifyConnections(ArrayModifications.GET_CURRENT_CONNECTIONS, null);
		System.out.println(c[0].hasNewMessage());
				c[0].sendMessage("Lol");
		System.out.println(Arrays.toString(c));
		System.out.println("K");
		printNewMessages(c[1]);
		System.out.println("aha");
		c[1].sendMessage("Test1");
		c[1].sendMessage("Test2");
		c[0].sendMessage("Go");
		printNewMessages(c[0]);
		printNewMessages(c[1]);
		printNewMessages(c[0]);
	}
	private void printNewMessages(Connection con) throws ConnectionError {
		if(con.isClosed()) Connections.modifyConnections(ArrayModifications.REMOVE_CONNECTION,con);
		while(con.hasNewMessage()) {
			System.out.println("1");
			System.out.println(con.getOtherName()+"	"+con.getNewMessage());
			System.out.println("2");
		}
		System.out.println("Escaped");
	}
	void newConnection() throws ConnectionError{
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
			try {
				Connections.modifyConnections(ArrayModifications.ADD_CONNECTION,new Connection(
							new Socket(InetAddress.getByAddress(ipBytes),
									Constants.STANDARD_PORT),
							"Neue", true));
			} catch (IOException e) {
				throw new ConnectionError("test".getBytes(),null);
			}
	}
}
