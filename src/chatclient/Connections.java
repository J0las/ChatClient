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

import java.util.ArrayList;

import chatclient.lib.ArrayModifications;
/*Static class for storing connections*/
class Connections {
	private final static ArrayList<Connection> connections= new ArrayList<Connection>();
	static synchronized Connection[] modifyConnections(ArrayModifications mode,Connection connection) {
		switch(mode) {
		case ADD_CONNECTION:
			/* Adds a new Connection to the end of the ArrayList and returns null*/
			connections.add(connection);
			break;
		case REMOVE_CONNECTION:
			/*Removes the specified connection from the ArrayList and returns null*/
			connections.remove(connection);
			break;
		case GET_CURRENT_CONNECTIONS:
			/*Returns an Array of the current connections in the ArrayList*/
			Connection[] connectionArray = new Connection[connections.size()];
			connections.toArray(connectionArray);
			return connectionArray;
		}	
		return null;
	}
}