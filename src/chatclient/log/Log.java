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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import chatclient.lib.ByteConverter;
import chatclient.lib.ChatMagicNumbers;

public class Log {
	private static boolean setUp = false;
	private static final int IP_PORT = 0;
	private static final int IP = 0;
	private static final int OTHER_NAME = 1;
	private static final int AES_KEY_HASH = 1;
	private static final int HEX_MESSAGE = 1;
	private static final int RAW_MESSAGE = 1;
	private static final int OWN_NAME = 2;
	private static final int MESSAGE_CONTENTS = 2;
	private static final int CALC_HASH = 2;
	private static final int SEND_HEADER = 2;
	private static final int MESSAGE_LENGTH = 2;
	private static final int SEND_HASH = 3;
	private static final int EXPECTED_HEADER = 3;
	private static final int CORRUPTED_MESSAGE = 4;
	
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
			System.err.println(LogLevel.INFO.getLogLevel()+		"\t"+LogLevel.INFO.getLogLevelDesc());
			System.err.println(LogLevel.ERROR.getLogLevel()+	"\t\t"+LogLevel.ERROR.getLogLevelDesc());
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
			if(args.length != 2) throw new IllegalArgumentException();
			sb.append("SHA256 over AES_KEY for Connection: ");
			sb.append(args[IP_PORT]);
			sb.append("\t");
			sb.append(args[AES_KEY_HASH]);
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
			sb.append(": ");
			sb.append(args[MESSAGE_CONTENTS]);
			break;
		case MESSAGE_SEND:
			if(args.length != 3) throw new IllegalArgumentException();
			sb.append("Sending message to: ");
			sb.append(args[IP_PORT]);
			sb.append(" / ");
			sb.append(args[OTHER_NAME]);
			sb.append(" content: ");
			sb.append(args[MESSAGE_CONTENTS]);
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
		case HASH_INVALID:
			if(args.length != 5) throw new IllegalArgumentException();
			sb.append("Hash check failed for connection to: ");
			sb.append(args[IP_PORT]);
			sb.append(" / ");
			sb.append(args[OTHER_NAME]);
			sb.append(" calculated hash: ");
			sb.append(args[CALC_HASH]);
			sb.append(" send hash: ");
			sb.append(args[SEND_HASH]);
			sb.append(" UTF_8 encoded message: ");
			sb.append(args[CORRUPTED_MESSAGE]);
			break;
		case HEADER_INVALID:
			if(args.length != 4) throw new IllegalArgumentException();
			sb.append("Header check failed for connection to: ");
			sb.append(args[IP_PORT]);
			sb.append(" / ");
			sb.append(args[OTHER_NAME]);
			sb.append(" send header: ");
			sb.append(args[SEND_HEADER]);
			sb.append(" expected header: ");
			sb.append(args[EXPECTED_HEADER]);
			sb.append(" abort Header: ");
			sb.append(ByteConverter.byteToHex(ChatMagicNumbers.CLOSE_CONNECTION));
			break;
		case PUB_KEY_INVALID:
			if(args.length != 1) throw new IllegalArgumentException();
			sb.append("Key exchange failed in connection: ");
			sb.append(args[IP_PORT]);
			sb.append(" because of an invalid public Key");
			break;
		case GENERAL_IO_ERROR:
			if(args.length != 1) throw new IllegalArgumentException();
			sb.append("A general io error has occurred in connection: ");
			sb.append(args[IP_PORT]);
			break;
		case UNREACHABLE_IP:
			if(args.length != 1) throw new IllegalArgumentException();
			sb.append("Could not reach specified ip: ");
			sb.append(args[IP]);
			break;
		case MESSAGE_FORMAT_INVALID:
			if(args.length != 2) throw new IllegalArgumentException();
			sb.append("Recieved a new message with an invalid format in connection: ");
			sb.append(args[IP_PORT]);
			sb.append(" hexmessage: ");
			sb.append(args[HEX_MESSAGE]);
			break;
		case BASE64_ENCODING_INVALID:
			if(args.length != 2) throw new IllegalArgumentException();
			sb.append("Recieved a new message with an invalid base64 encoding in connection: ");
			sb.append(args[IP_PORT]);
			sb.append(" rawmessage: ");
			sb.append(args[RAW_MESSAGE]);
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
}
