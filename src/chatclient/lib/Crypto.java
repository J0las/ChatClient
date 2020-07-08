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

import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {
	private Cipher cipher;
	private byte[] encodedAES_Params;
	private SecretKeySpec AES_Key;
	public Crypto(SecretKeySpec AES_Key, byte[] encodedAES_Params){
		this.AES_Key = AES_Key;
		this.encodedAES_Params = Arrays.copyOf(encodedAES_Params, encodedAES_Params.length);
		try {
			this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			/*This should never happen*/
			throw new AssertionError();
		}
	}
	public synchronized byte[] cryptoOperation(byte[] data, CryptoModes mode) {
		switch(mode) {
		/*Case for Encrypting the input data*/
		case ENCRYPT:
			try {
					AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
										params.init(encodedAES_Params);
					cipher.init(Cipher.ENCRYPT_MODE, AES_Key, params);
				/*Performs the AES encryption on the given data*/
				return cipher.doFinal(Arrays.copyOfRange(data, Constants.CHECKSUM_OFFSET, data.length));
				
			} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
						IOException e) {
				/*This should never happen*/
				e.printStackTrace();
			}
		 catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
			throw new AssertionError();
		}
		/*Case for Decrypting the input data*/
		case DECRYPT:
			try {
				/*Initializes the Cipher with the exchanged AES_Key in decryption mode*/
				AlgorithmParameters params = AlgorithmParameters.getInstance("AES");
									params.init(encodedAES_Params);
				cipher.init(Cipher.DECRYPT_MODE, AES_Key,params);
				/*Performes the AES decryption on the given data*/
				data = cipher.doFinal(Arrays.copyOfRange(data, Constants.CHECKSUM_OFFSET, data.length));
				return data;
			} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
					 IOException e) {
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
				throw new AssertionError();
			}
		default:
			throw new IllegalArgumentException();
		}
	}
}
