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

import java.io.IOException;
import java.net.ServerSocket;

import chatclient.lib.ArrayModifications;
import chatclient.lib.ConnectionError;
import chatclient.lib.Constants;
/*Server for accepting incoming connections*/
class Server extends Thread {
	/*Name of this ChatClient, passed to the other side of the connection*/
	private String name="";
	ServerSocket server;
	Server(String name){
		/*Sets the name of the server*/
		this.name=name;
	}
	/*Starts the server*/
	@Override
	public void run() {
		try {
			server = new ServerSocket(Constants.STANDARD_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*Adds a shutdownhook to safely close the server on program termination*/
		Runtime.getRuntime().addShutdownHook(new ShutDownConnections(server));
		System.out.println("Started server");
		/*Loop for accepting new connections*/
		while(true) {
			try {
				/*Adds the new connection in accepting configuration to the shared ArrayList*/
				Connections.modifyConnections(ArrayModifications.ADD_CONNECTION,
							new Connection(server.accept(),name,false));
			} catch (ConnectionError | IOException e) {
				e.printStackTrace();
			}
		}
	}	
}
