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
import java.util.Base64;

import chatclient.lib.ChatMagicNumbers;

public class Log {
	private static boolean setUp = false;
	private static final int IP_PORT = 0;
	private static final int OTHER_NAME = 1;
	private static final int OWN_NAME = 2;
	private static final int AES_KEY_HASH = 3;
	private static final int BASE64_MESSAGE = 2;
	private static final int RAW_MESSAGE = 2;
	
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
		if(args == null) throw new IllegalArgumentException();
		StringBuilder sb = new StringBuilder();
		sb.append(logType.getLogLevel().getLogLevel());
		sb.append(" \t");
		sb.append(getCurrentDateTime());
		sb.append("\t");
		switch(logType) {
		case INCOMMING_CONNECTION:
			if(args.length != 1) throw new IllegalArgumentException();
			sb.append("Incomming new Connection from: ");
			sb.append(args[IP_PORT]);
			break;
		case CONNECTION_CLOSED:
			if(args.length != 1) throw new IllegalArgumentException();
			sb.append("Connection to: ");
			sb.append(args[IP_PORT]);
			sb.append(" was closed.");
			break;
		case AES_KEY_HASH:
			if(args.length != 4) throw new IllegalArgumentException();
			sb.append("SHA256 over AES_KEY for Connection: ");
			sb.append(args[IP_PORT]);
			sb.append(" from: ");
			sb.append(args[OWN_NAME]);
			sb.append(" to: ");
			sb.append(args[OTHER_NAME]);
			sb.append("\t");
			byteArrayToHexString(sb,Base64.getDecoder().decode(args[AES_KEY_HASH].getBytes(StandardCharsets.UTF_8)));
			break;
		case CREATED_NEW_CONNECTION:
			if(args.length != 1) throw new IllegalArgumentException();
			sb.append("Created new Connection to: ");
			sb.append(args[IP_PORT]);
			break;
		case KEY_EXCHANGE_FAILED:
			if(args.length != 3) throw new IllegalArgumentException();
			sb.append("key exchange failed in Connection: ");
			sb.append(args[IP_PORT]);
			sb.append(" other name: ");
			sb.append(args[OTHER_NAME]);
			sb.append(" own name: ");
			sb.append(args[OWN_NAME]);
			break;
		case MESSAGE_RECIEVED:
			if(args.length != 3) throw new IllegalArgumentException();
			sb.append("Recieved a new message from: ");
			sb.append(args[IP_PORT]);
			sb.append(" / ");
			sb.append(args[OTHER_NAME]);
			sb.append(" Base64: ");
			sb.append(args[BASE64_MESSAGE]);
			sb.append(" / ");
			sb.append(Arrays.toString(Base64.getDecoder().decode(
					args[BASE64_MESSAGE].getBytes(StandardCharsets.UTF_8))));
			sb.append(" Expected header: "+ChatMagicNumbers.ENC_MESSAGE);
			break;
		case MESSAGE_SEND:
			if(args.length != 3) throw new IllegalArgumentException();
			sb.append("Sending message to: ");
			sb.append(args[IP_PORT]);
			sb.append(" / ");
			sb.append(args[OTHER_NAME]);
			sb.append(" content: ");
			sb.append(args[RAW_MESSAGE]);
			break;
		case TEST_STING_DECRYPTION_FAILED:
			if(args.length != 1) throw new IllegalArgumentException();
			sb.append("Test String decryption failed in connection: ");
			sb.append(args[IP_PORT]);
			break;
		case CONNECTION_ESTABLISHED:
			if(args.length != 2) throw new IllegalArgumentException();
			sb.append("Successfully established a connection to: ");
			sb.append(args[IP_PORT]);
			sb.append(" / ");
			sb.append(args[OTHER_NAME]);
			break;
		default:
			sb.append("Illegal arguments pased!");
			System.err.println(sb.toString());
			throw new IllegalArgumentException();
		}
		System.err.println(sb.toString());
	}
	private static String getCurrentDateTime() {
		return LocalDateTime.now().format(DateTimeFormatter.ofPattern("<yyyy-MM-dd | HH:mm:ss>"));
	}
	private static void byteArrayToHexString(StringBuilder sb, byte[] bytes) {
		char[] hexDigits = new char[2];
		sb.append("0x");
		for(byte b : bytes) {
			hexDigits[0] = Character.forDigit((b >> 4) & 0xF, 16);
		    hexDigits[1] = Character.forDigit((b & 0xF), 16);
		    sb.append(String.valueOf(hexDigits).toUpperCase());
		}
	}
}
