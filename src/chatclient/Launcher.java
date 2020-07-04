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

import javax.swing.JOptionPane;

import chatclient.gui.AnmeldeFenster;
import chatclient.lib.ConnectionError;

public class Launcher {
	
	public static String name;
	private static Thread 	server;
	public static volatile boolean loggedIn = false;
	
	public static void main(String[] args) throws ConnectionError {
	    ensureSingleInstance();
	    AnmeldeFenster frame = new AnmeldeFenster();
        frame.setVisible(true);
        while(!loggedIn);
        frame.dispose();
		/*Creates a server with the ChatClientName*/
		server = new Server(name);
		/*Starts the server thread*/
		server.start();
		Client.run();
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
					System.out.println("Failed to release the lock");
				}
                file.delete();					
			}));
				return;
			}	
		} catch (IOException e) {
			System.out.println("Failed to acquire the lock");
			return;
		}
		JOptionPane.showMessageDialog(null,
                "An instance of this programm is allready running!",
                "Multiple instance error",					      
                JOptionPane.ERROR_MESSAGE);
		System.exit(1);
	}
	
}