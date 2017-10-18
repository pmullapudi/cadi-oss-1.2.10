/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Hash {
	private static char hexDigit[] = "0123456789abcdef".toCharArray();
	
/////////////////////////////////
// MD5
/////////////////////////////////
	/**
	 * Encrypt MD5 from Byte Array to Byte Array
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] encryptMD5 (byte[] input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(input); 
		return md.digest();
	}

	/**
	 * Encrypt MD5 from Byte Array to Byte Array
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static byte[] encryptMD5 (byte[] input, int offset, int length) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(input,offset,length); 
		return md.digest();
	}



	/**
	 * Convenience Function: Encrypt MD5 from String to String Hex representation 
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String encryptMD5asStringHex(String input) throws NoSuchAlgorithmException {
		byte[] output = encryptMD5(input.getBytes());
		StringBuilder sb = new StringBuilder("0x");
		 for (byte b : output) {
		    sb.append(hexDigit[(b >> 4) & 0x0f]);
		    sb.append(hexDigit[b & 0x0f]);
		 }
		 return sb.toString();
	}

/////////////////////////////////
// SHA256
/////////////////////////////////
	/**
	 * SHA256 Hashing
	 */
	public static byte[] hashSHA256(byte[] input) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(input); 
		return md.digest();
	}

	/**
	 * SHA256 Hashing
	 */
	public static byte[] hashSHA256(byte[] input, int offset, int length) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(input,offset,length); 
		return md.digest();
	}
	
	/**
	 * Convenience Function: Hash from String to String Hex representation
	 * 
	 * @param input
	 * @return
	 * @throws NoSuchAlgorithmException
	 */
	public static String hashSHA256asStringHex(String input) throws NoSuchAlgorithmException {
		byte[] output = hashSHA256(input.getBytes());
		StringBuilder sb = new StringBuilder("0x");
		 for (byte b : output) {
		    sb.append(hexDigit[(b >> 4) & 0x0f]);
		    sb.append(hexDigit[b & 0x0f]);
		 }
		 return sb.toString();
	}

	/**
	 * Compare two byte arrays for equivalency
	 * @param ba1
	 * @param ba2
	 * @return
	 */
	public static boolean isEqual(byte ba1[], byte ba2[]) {
		if(ba1.length!=ba2.length)return false;
		for(int i = 0;i<ba1.length; ++i) {
			if(ba1[i]!=ba2[i])return false;
		}
		return true;
	}

	public static int compareTo(byte[] a, byte[] b) {
		int end = Math.min(a.length, b.length);
		int compare = 0;
		for(int i=0;compare == 0 && i<end;++i) {
			compare = a[i]-b[i];
		}
		if(compare==0)compare=a.length-b.length;
		return compare;
	}
	
	public static String toHex(byte[] ba) {
		StringBuilder sb = new StringBuilder("0x");
		 for (byte b : ba) {
		    sb.append(hexDigit[(b >> 4) & 0x0f]);
		    sb.append(hexDigit[b & 0x0f]);
		 }
		 return sb.toString();
	}
	
	public static byte[] fromHex(String s)  throws CadiException{
		if(!s.startsWith("0x")) {
			throw new CadiException("HexString must start with \"0x\"");
		}
		boolean high = true;
		int c;
		byte b;
		byte[] ba = new byte[(s.length()-2)/2];
		int idx;
		for(int i=2;i<s.length();++i) {
			c = s.charAt(i);
			if(c>=0x30 && c<=0x39) {
				b=(byte)(c-0x30);
			} else if(c>=0x61 && c<=0x66) {
				b=(byte)(c-0x57);  // account for "A"
			} else if(c>=0x41 && c<=0x46) {
				b=(byte)(c-0x37);
			} else {
				throw new CadiException("Invalid char '" + c + "' in HexString");
			}
			idx = (i-2)/2;
			if(high) {
				ba[idx]=(byte)(b<<4);
				high = false;
			} else {
				ba[idx]|=b;
				high = true;
			}
		}
		return ba;
	}

}
