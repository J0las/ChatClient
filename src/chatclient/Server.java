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
import java.net.ServerSocket;
import java.net.Socket;

import chatclient.lib.ArrayModifications;
import chatclient.lib.ConnectionError;
import chatclient.lib.Constants;
import chatclient.log.Log;
import chatclient.log.LogType;
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
		}/*Adds a shutdownhook to safely close the server on program termination*/
		Runtime.getRuntime().addShutdownHook(new ShutDownHandler(server));
		System.out.println("Started server");
		/*Loop for accepting new connections*/
		while(true) {
			try {
				/*Logs the incoming connection*/
				Socket s = server.accept();
				Log.log(new String[] {
					s.getInetAddress().getHostAddress()+"/"+s.getLocalPort()},
				LogType.INCOMMING_CONNECTION);
				/*Adds the new connection in accepting configuration to the shared ArrayList*/
				Connections.modifyConnections(ArrayModifications.ADD_CONNECTION,
							new Connection(s,name,false));
			} catch (ConnectionError | IOException e) {
				e.printStackTrace();
			}
		}
	}	
}
