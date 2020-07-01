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

package chatclient.lib;

public class ByteConverter {

	public static String byteToHex( byte b) {
		StringBuilder sb = new StringBuilder();
		sb.append("0x");
		sb.append(String.copyValueOf(new char[] {
				Character.forDigit((b >> 4) & 0xF, 16),
				Character.forDigit((b & 0xF), 16)
		}).toUpperCase());
		return sb.toString();
	}
	public static String byteArrayToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		char[] hexDigits = new char[2];
		sb.append("0x");
		for(byte b : bytes) {
			hexDigits[0] = Character.forDigit((b >> 4) & 0xF, 16);
		    hexDigits[1] = Character.forDigit((b & 0xF), 16);
		    sb.append(String.valueOf(hexDigits).toUpperCase());
		}
		return sb.toString();
	}
}
