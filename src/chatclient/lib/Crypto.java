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
			this.cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
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
				return cipher.doFinal(data, Constants.CHECKSUM_OFFSET,
												data.length-Constants.HEADER_SIZE);
				
			} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
						IOException | InvalidAlgorithmParameterException | NoSuchAlgorithmException e) {
				/*This should never happen*/
				e.printStackTrace();
			}
		/*Case for Decrypting the input data*/
		case DECRYPT:
			try {
				/*Initializes the Cipher with the exchanged AES_Key in decryption mode*/
				AlgorithmParameters params		= AlgorithmParameters.getInstance("AES");
									params.init(encodedAES_Params);
				cipher.init(Cipher.DECRYPT_MODE, AES_Key,params);
				/*Performes the AES decryption on the given data*/
				return cipher.doFinal(data, Constants.CHECKSUM_OFFSET,
												data.length-Constants.HEADER_SIZE);
			} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException |
						InvalidAlgorithmParameterException | IOException | NoSuchAlgorithmException e) {
				/*This should never happen*/
				e.printStackTrace();
			}
		}
		return null;
	}
}
