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
 *  This class stores all established connection in a central storage
 *  allowing access from multiple threads to store remove or get a 
 *  array of the connection 
 */

package chatclient;

import java.util.Vector;

public class Connections {
    
    /*
     *  Vector for storing the connections
     * 
     *  Using vector instead of arraylist as vector is thread safe
     */
	private final static Vector<Connection> connections= new Vector<Connection>();
	
	/*Adds a connection to the vector*/
	public static void add(Connection con) {
		connections.add(con);
	}
	
	/*Removes a connection from the vector*/
	public static void remove(Connection con) {
		connections.remove(con);
	}
	
	/*Returns all currently stored connections as an array*/
	public static Connection[] toArray() {
		return connections.toArray(new Connection[] {});
	}
}