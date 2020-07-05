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
/*Magic numbers to identify the type of message*/
public class ChatMagicNumbers{
	public final static byte HELLO 					= 0x0F;	//Send on first connection
	/*Returned from Server if connection is accepted*/
	public final static byte ACCEPTED_CONNECTION 	= 0x1F;	
	public final static byte PUBLIC_KEY 			= 0x2F;	//Public Key of the sender
	public final static byte ENCODED_PARAMS			= 0x3F;	//AES-Parameter from the server
	/*Contains a known teststring to test the encryption*/
	public final static byte ENC_TEST_STRING		= 0x4F; 
	/*Finalizes the keygen and switches to AES encryption with the generated key*/
	public final static byte SWITCH_TO_ENC_MODE		= 0x5F;
	public final static byte CONNECTION_NAME		= (0x6F); //Name of the connection Partner
	public final static byte ENC_MESSAGE			= (byte)(0x7F); //Encrypted Message
	public final static byte ENC_SUCCESS            = (byte)(0xE0); //Teststring decryption succeeded
	/*The teststring could not be decrypted*/
	public final static byte ENC_ERROR				= (byte)(0xF0);
	public final static byte CLOSE_CONNECTION		= (byte)(0xFF);
}