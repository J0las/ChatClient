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
/*Safely shuts down the server on program termination*/
class ShutDownServer extends Thread {
	private ServerSocket server;
	ShutDownServer(ServerSocket server){
		this.server = server;
	}
	@Override
	public void run() {
		System.out.println("Closing server");
		if(server.isClosed()) return;
		try {
			server.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
