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
/*Safely shuts down the server and open connections on program termination*/
class ShutDownConnections extends Thread {
	private ServerSocket server;
	ShutDownConnections(ServerSocket server){
		this.server = server;
	}
	@Override
	public void run() {
		/*Closes the server*/
		System.out.println("Closing server");
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		/*Closes all open connections*/
		System.out.println("Closing existing connections");
		Connection[] cons = Connections.modifyConnections(ArrayModifications.GET_CURRENT_CONNECTIONS, null);
		for(Connection con : cons) {
			con.closeConnection();
		}
	}
}
