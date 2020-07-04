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
 *  This class is responsible for shutting down the ServerSocket and all open Connections
 *  on program termination
 */

package chatclient;

import java.io.IOException;

import chatclient.log.Log;
import chatclient.log.LogType;

class ShutDownHandler extends Thread {
    /*Stores the server object for eventual closing*/
	private Server server;
	
	/*Constructor stores the passed Server object*/
	ShutDownHandler(Server server){
		this.server = server;
	}
	
	/*Runs the Thread to shutdown the open connections and the server*/
	@Override
	public void run() {
		/*Closes the server*/
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		server.interrupt();
		Log.log(new String[0], LogType.CLOSED_SERVER);
		/*Get all current connections*/
		Connection[] cons = Connections.toArray();
		for(Connection con : cons) {
		    /*Close the selected connection*/
			con.closeConnectiom();
		}
		Log.close();
	}
}