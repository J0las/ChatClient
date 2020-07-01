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

public enum LogType {
	KEY_EXCHANGE_FAILED(LogLevel.ERROR),
	TEST_STING_DECRYPTION_FAILED(LogLevel.ERROR),
	HASH_INVALID(LogLevel.ERROR),
	HEADER_INVALID(LogLevel.ERROR),
	PUB_KEY_INVALID(LogLevel.ERROR),
	GENERAL_IO_ERROR(LogLevel.ERROR),
	UNREACHABLE_IP(LogLevel.ERROR),
	MESSAGE_FORMAT_INVALID(LogLevel.ERROR),
	BASE64_ENCODING_INVALID(LogLevel.ERROR),
	CONNECTION_CLOSED(LogLevel.INFO),
	AES_KEY_HASH(LogLevel.INFO),
	MESSAGE_SEND(LogLevel.INFO),
	MESSAGE_RECIEVED(LogLevel.INFO),
	CREATED_NEW_CONNECTION(LogLevel.INFO),
	INCOMMING_CONNECTION(LogLevel.INFO),
	CONNECTION_ESTABLISHED(LogLevel.INFO);
	
	private LogLevel logLevel;
	
	LogType(LogLevel logLevel) {
		this.logLevel = logLevel;
	}
	public LogLevel getLogLevel() {
		return logLevel;
	}
}
