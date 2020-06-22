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

import java.util.Scanner;

import chatclient.lib.ConnectionError;
import chatclient.log.Log;

public class Launcher {
	private static Thread 	server;
	private static String 	name 	= "Johan";
	private static Client client	= new Client(name);
	private static Scanner sc = new Scanner(System.in);
	public static void main(String[] args) throws ConnectionError {
		Log.init(true);
		/*Creates a server with the ChatClientName "Johan"*/
		server = new Server("Johan");
		/*Starts the server thread*/
		server.start();
		client.run();
		}
}