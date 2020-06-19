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

package chatclient;

import java.net.InetAddress;
import java.net.Socket;
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
		while(true) {
			Connection[] cons =
					Connections.modifyConnections(ArrayModifications.GET_CURRENT_CONNECTIONS,null);
			for(Connection con : cons) {
				printNewMessages(con);
			}
		}
	}
	private void printNewMessages(Connection con) throws ConnectionError {
		if(con.isClosed()) Connections.modifyConnections(ArrayModifications.REMOVE_CONNECTION,con);
		while(con.hasNewMessage()) {
			System.out.println(con.getName()+"	"+con.getNewMessage());
		}
	}
	void newConnection() throws Exception{
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
			Connections.modifyConnections(ArrayModifications.ADD_CONNECTION,new Connection(
						new Socket(InetAddress.getByAddress(ipBytes),
								Constants.STANDARD_PORT),
						name, true));
	}
}
