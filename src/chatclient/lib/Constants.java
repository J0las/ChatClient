/*
 *  Copyright (C) 2020  Felix Johannsmann, Johan Bücker
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
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

public class Constants {
	public static final String APP_ID 			= "ChatClient";
	public static final String TEST_STRING 	= "Success!";
	public static final int 	STANDARD_PORT 	= 53545;
	public static final int	HEADER_OFFSET	= 0;
	public static final int	HEADER_SIZE		= 1;
	public static final int	DH_KEY_SIZE		= 4096;
	public static final int	CHECKSUM_OFFSET	= HEADER_OFFSET + HEADER_SIZE;
	public static final int	CHECKSUM_SIZE	= 32; //256Bit
	public static final int	MESSAGE_OFFSET	= CHECKSUM_OFFSET + CHECKSUM_SIZE;
	public static final int	AES_KEY_LENGTH	= 32; //256Bit
}
