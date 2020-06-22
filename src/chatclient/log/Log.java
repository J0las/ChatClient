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

public class Log {
	Log(boolean enabled){
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
			System.err.println(LocalDateTime.now()+"\tStarted logging");
			System.err.println(LogLevel.INFO.getLogLevel()+		":\t"+LogLevel.INFO.getLogLevelDesc());
			System.err.println(LogLevel.ERROR.getLogLevel()+	":\t"+LogLevel.ERROR.getLogLevelDesc());
		} else {
			System.setErr(new PrintStream(OutputStream.nullOutputStream()));
		}
	}
	void log(byte[] message, LogLevel logLevel) {
		StringBuilder sb = new StringBuilder();
		sb.append(logLevel.getLogLevel()+"\t");
		sb.append(LocalDateTime.now());
		sb.append("\t");
		switch(logLevel) {
		case INFO:
			sb.append()
		}
	}
}
