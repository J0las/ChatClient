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

import java.awt.Rectangle;
import java.nio.charset.StandardCharsets;

public class Constants {
	public static final byte[] 	TEST_BYTES 		= "Success!".getBytes(StandardCharsets.UTF_8);
	public static final int 	STANDARD_PORT 	= 53545;
	public static final int		HEADER_OFFSET	= 0;
	public static final int		HEADER_SIZE		= 1;
	public static final int		DH_KEY_SIZE		= 4096;
	public static final int		CHECKSUM_OFFSET	= HEADER_OFFSET + HEADER_SIZE;
	public static final int		CHECKSUM_SIZE	= 32; //256Bit
	public static final int		MESSAGE_OFFSET	= CHECKSUM_OFFSET + CHECKSUM_SIZE;
	public static final int		AES_KEY_LENGTH	= 32; //256Bit
	public static final Rectangle username_feld = new Rectangle(78, 11, 200, 20);
	public static final Rectangle anmeldebutton = new Rectangle(118, 47, 120, 23);
	public static final Rectangle nametextfeld  = new Rectangle(22, 5, 46, 33);
	public static final Rectangle logging_box   = new Rectangle(141, 77, 97, 23);
	public static final Rectangle chatfenster   = new Rectangle(100, 100, 703, 449);
	public static final Rectangle connectButton = new Rectangle(182, 333, 108, 77);
	public static final Rectangle ip_anweisung  = new Rectangle(10, 333, 273, 38);
	public static final Rectangle chattext      = new Rectangle(310, 60, 370, 263);
	public static final Rectangle send_feld     = new Rectangle(310, 333, 270, 78);
	public static final Rectangle sendeButton   = new Rectangle(590, 333, 90, 77);
}
