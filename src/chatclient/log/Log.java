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

package chatclient.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class Log {
	private static boolean setUp = false;
	public static void init (boolean enabled){
		if(enabled) {
			File file = new File(System.getProperty("user.home") + "\\Desktop\\ChatClient.log");
			if(file.exists()) file.delete();
			try {
				file.createNewFile();
			} catch (IOException e) {
				throw new AssertionError();
			}
			try {
				PrintStream fileOut = new PrintStream(file);
				System.setErr(fileOut);
			} catch (FileNotFoundException e) {
				throw new AssertionError();
			}
			System.err.println(getCurrentDateTime()+"\tStarted logging");
			System.err.println(LogLevel.INFO.getLogLevel()+		":\t\t"+LogLevel.INFO.getLogLevelDesc());
			System.err.println(LogLevel.ERROR.getLogLevel()+	":\t"+LogLevel.ERROR.getLogLevelDesc());
		} else {
			System.setErr(new PrintStream(OutputStream.nullOutputStream()));
		}
		setUp = true;
	}
	public static void log(String[] args, LogType logType) {
		if(!setUp) System.exit(0);
		StringBuilder sb = new StringBuilder();
		sb.append(logType.getLogLevel().getLogLevel());
		sb.append(" \t");
		sb.append(getCurrentDateTime());
		sb.append("\t");
		switch(logType) {
		case INCOMMING_CONNECTION:
			if(args.length!=3) log(null, LogType.ILLEGAL_ARGUMENT_ERROR);
			sb.append();
			break;
		case CONNECTION_CLOSED:

		case AES_KEY_HASH:
			break;
		case CREATED_NEW_CONNECTION:
			break;
		case KEY_EXCHANGE_FAILED:
			break;
		case MESSAGE_RECIEVED:
			break;
		case MESSAGE_SEND:
			break;
		case TEST_STING_DECRYPTION_FAILED:
			break;
		case ILLEGAL_ARGUMENT_ERROR:
			
			break;
		default:
			throw new IllegalArgumentException();
		}
	}
	private static String getCurrentDateTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("<yyyy-MM-dd | HH:mm:ss>"));
	}
}
