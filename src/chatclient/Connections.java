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

import java.util.Vector;

/*Static class for storing connections*/
class Connections {
	private final static Vector<Connection> connections= new Vector<Connection>();
	public static void add(Connection con) {
		connections.add(con);
	}
	public static void remove(Connection con) {
		connections.remove(con);
	}
	public static Connection[] toArray() {
		return connections.toArray(new Connection[] {});
	}
}