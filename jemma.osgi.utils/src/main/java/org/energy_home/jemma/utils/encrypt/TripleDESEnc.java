/**
 * This file is part of JEMMA - http://jemma.energy-home.org
 * (C) Copyright 2013 Telecom Italia (http://www.telecomitalia.it)
 *
 * JEMMA is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License (LGPL) version 3
 * or later as published by the Free Software Foundation, which accompanies
 * this distribution and is available at http://www.gnu.org/licenses/lgpl.html
 *
 * JEMMA is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License (LGPL) for more details.
 *
 */
package org.energy_home.jemma.utils.encrypt;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class TripleDESEnc {
    public static TripleDESEnc getInstance(String password) {
    	return new TripleDESEnc(password);
    }

    private Cipher encryptCipher;
    private Cipher decryptCipher;
    
    private static byte[] copyOf(byte[] original, int newLength) {
    	byte[] res = new byte[newLength];
    	int min = 0;
    	if (original != null) {
    		min = Math.min(newLength, original.length);
    		System.arraycopy(original, 0, res, 0, min);
    	}
    	
    	for (int i = min; i < newLength; i++) {
			res[i] = 0;
		}
    	
    	return res;
    }
        
    private TripleDESEnc(String password) {
    	try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] digestOfPassword = md.digest(password.getBytes("utf-8"));
	        
	        
	        // Original code  
	        //keyBytes = Arrays.copyOf(digestOfPassword, 24);

            byte[] keyBytes = copyOf(digestOfPassword, 24);
	        
	        for (int j = 0, k = 16; j < 8;) {
	                keyBytes[k++] = keyBytes[j++];
	        }
            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            IvParameterSpec iv = new IvParameterSpec(new byte[8]);
			encryptCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			encryptCipher.init(Cipher.ENCRYPT_MODE, key, iv);
			decryptCipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
			decryptCipher.init(Cipher.DECRYPT_MODE, key, iv);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}  catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
    }

    public synchronized byte[] encrypt(String message) {       
        byte[] plainTextBytes;

		try {
			plainTextBytes = message.getBytes("utf-8");
			return encryptCipher.doFinal(plainTextBytes);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public String hexEncrypt(String message) {
		byte[] bytes  = encrypt(message);
		if (bytes == null)
			return null;
		return Bytes.toHex(bytes);
    }

    public synchronized String decrypt(byte[] message) {
        byte[] plainText;
		try {
			plainText = decryptCipher.doFinal(message);
			return new String(plainText, "UTF-8");
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
    }
    
    public String hexDecrypt(String message) {
		return decrypt(Bytes.fromHex(message)); 
    }
    
    public static void main(String[] args) throws Exception {
    	TripleDESEnc codec = TripleDESEnc.getInstance("m4ch1n3t0m4ch1n3c0nn3ct10nh0m34ut0m4t10np0rt4l3n3rgy4th0m3");
        String encodedText;
        String decodedText;
        String text;
        System.out.println(System.currentTimeMillis() + ": starting encoding\n\n\n");
        for (int i = 0; i < 10000; i++) {
			if (i == 0)
				text = "a";
			else
				text = "cid-"+String.format("%04d", i);

	        encodedText = codec.hexEncrypt(text);
	        System.out.println("Encoded text: " + encodedText);
	      
	        decodedText = codec.hexDecrypt(encodedText);
	        System.out.println("Decoded text: " + decodedText + "\n");

		}
        System.out.println("\n\n\n" + System.currentTimeMillis() + ": finished decoding ");

    }
}

