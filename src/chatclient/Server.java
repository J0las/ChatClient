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
 *  This class is used for accepting incoming new connections.
 *  A Thread is used as server.accept() blocks until a new connection
 *  can be returned and program execution would halt.
 */

package chatclient;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import chatclient.lib.ComponendAlreadyRunningError;
import chatclient.lib.ConnectionError;
import chatclient.lib.Constants;
import chatclient.log.Log;
import chatclient.log.LogType;

public class Server extends Thread {
	/*Name of this ChatClient, passed to the other side of the connection*/
	private String name="";
	/*ServerSocket object for accepting new connections*/
	ServerSocket server;
	/*boolean to check if this class of object has been constructed already*/
	private static volatile boolean alreadyRunning = false;
	
	/*Constructor sets up this server*/
	Server(String name){
	    if(alreadyRunning) throw new ComponendAlreadyRunningError(this);
	    alreadyRunning = true;
	}
	
	/*Starts the server thread*/
	@Override
	public void run() {
	    /*Try to create the server*/
		try {
			server = new ServerSocket(Constants.STANDARD_PORT);
		} catch (IOException e) {
		    /*Log the exception*/
			Log.log(new String[] {
			        e.getMessage()
			}, LogType.FAILED_TO_CREATE_SERVER);
			/*Terminate the program*/
			System.exit(1);
		}
		/*Log the successful creation of the server*/
		Log.log(new String[0], LogType.CREATED_SERVER);
		/*Adds a shutdownhook to safely close the server on program termination*/
		Runtime.getRuntime().addShutdownHook(new ShutDownHandler(this));
		/*Loop for accepting new connections until thread is interupted*/
		while(!isInterrupted()) {
			try {
			    /*Accept a incoming connections socket*/
				Socket s = server.accept();
				/*create a connection object with the accepted socket*/
				Connection con = new Connection(s, false);
				/*Start the Thread of this connection to get the messages*/
				con.start();
				/*Adds the new connection in accepting configuration to the shared ArrayList*/
				Connections.add(con);
			} catch (ConnectionError | IOException e) {}
		}
	}	
	ServerSocket getServerSocket() {
	    return server;
	}
}