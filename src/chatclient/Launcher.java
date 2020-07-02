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

import chatclient.lib.ConnectionError;
import chatclient.log.Log;

public class Launcher {
	
	private static Thread 	server;
	private static String 	name 	= "Johan";
	private static Client client	= new Client(name);
	
	public static void main(String[] args) throws ConnectionError {
		ensureSingleInstance();
		Log.init(true);
		/*Creates a server with the ChatClientName "Johan"*/
		server = new Server("Johan");
		/*Starts the server thread*/
		server.start();
		client.run();
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