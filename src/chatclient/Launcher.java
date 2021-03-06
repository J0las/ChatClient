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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import chatclient.gui.AnmeldeFenster;
import chatclient.gui.ChatFenster;
import chatclient.gui.ErrorFenster;
import chatclient.lib.ConnectionError;

public class Launcher {
	
	public static String name;
	private static Thread 	server;
	public static volatile boolean loggedIn = false;
	public static volatile Connection selectedConnection = null;
	private static int indexLastConnection = 0;
	static Connection[] connections = new Connection[5];
	public static ChatFenster chatFenster;
	
	public static void main(String[] args) throws ConnectionError {
	    ensureSingleInstance();
	    AnmeldeFenster anmeldeFenster = new AnmeldeFenster();
        anmeldeFenster.setVisible(true);
        while(!loggedIn);
        anmeldeFenster.dispose();
		/*Creates a server with the ChatClientName*/
		server = new Server();
		/*Starts the server thread*/
		server.start();
		chatFenster = new ChatFenster();
        chatFenster.setVisible(true);
		}
	private static void ensureSingleInstance() {
		final File file = new File(System.getProperty("java.io.tmpdir")+"ChatClient.lock");
		try {
			file.createNewFile();
		    final RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			final FileLock fileLock = randomAccessFile.getChannel().tryLock();
			if(fileLock != null) {
				Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
					fileLock.release();
					randomAccessFile.close();
				} catch (IOException e) {
					ErrorFenster.error("Failed to release the lock");
				}
                file.delete();					
			}));
				return;
			}	
		} catch (IOException e) {
			ErrorFenster.error("Failed to aquire the lock");
			return;
		}
		ErrorFenster.error("An instance of this programm is allready running!");
		System.exit(1);
	}
	public static synchronized void array(Connection con) {
	    if(!(connections[Launcher.indexLastConnection] == null)) {
	        connections[Launcher.indexLastConnection].closeConnection(true);
	    }
	    connections[Launcher.indexLastConnection] = con;
	    Launcher.chatFenster.getButton(Launcher.indexLastConnection).setText(con.getOtherName());
	    Launcher.chatFenster.add(con.getJTextPane());
        Launcher.chatFenster.getButton(Launcher.indexLastConnection).addActionListener(con);
	    Launcher.indexLastConnection = (Launcher.indexLastConnection+1)%5;
	}
	static int getArrayIndex(Connection con) {
	    for(int i=0; i<5; i++) {
	        if(connections[i]==con) return i;
	    }
	    return 5;
	}    
}