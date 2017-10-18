/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import org.junit.Test;

import com.att.cadi.Symm;
import com.att.cadi.config.Config;


public class JU_Base64 {
	private static final String encoding = "Man is distinguished, not only by his reason, but by this singular " +
			"passion from other animals, which is a lust of the mind, that by a " + 
			"perseverance of delight in the continued and indefatigable generation of " + 
			"knowledge, exceeds the short vehemence of any carnal pleasure.";
		 
	private static final String expected = 
			"TWFuIGlzIGRpc3Rpbmd1aXNoZWQsIG5vdCBvbmx5IGJ5IGhpcyByZWFzb24sIGJ1dCBieSB0aGlz\n" + 
			"IHNpbmd1bGFyIHBhc3Npb24gZnJvbSBvdGhlciBhbmltYWxzLCB3aGljaCBpcyBhIGx1c3Qgb2Yg\n" + 
			"dGhlIG1pbmQsIHRoYXQgYnkgYSBwZXJzZXZlcmFuY2Ugb2YgZGVsaWdodCBpbiB0aGUgY29udGlu\n" +
			"dWVkIGFuZCBpbmRlZmF0aWdhYmxlIGdlbmVyYXRpb24gb2Yga25vd2xlZGdlLCBleGNlZWRzIHRo\n" +
			"ZSBzaG9ydCB2ZWhlbWVuY2Ugb2YgYW55IGNhcm5hbCBwbGVhc3VyZS4=";


	@Test
	public void test() throws Exception {
		// Test with different Padding
		encode("leas",    "bGVhcw==");
		encode("leasu",   "bGVhc3U=");
		encode("leasur",  "bGVhc3Vy");
		encode("leasure", "bGVhc3VyZQ==");
		encode("leasure.","bGVhc3VyZS4=");

		// Test with line ends
		encode(encoding, expected);
		
		int ITER = 2000;
		System.out.println("Priming by Encoding Base64 " + ITER + " times");
		long start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.encode(encoding);
		}
		Float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
		
		System.out.println("Priming by Decoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.decode(expected);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");

		
		ITER=30000;
		System.out.println("Encoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.encode(encoding);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
		
		System.out.println("Decoding Base64 " + ITER + " times");
		start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.decode(expected);
		}
		ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");
	}
	
	@Test
	public void symmetric() throws IOException {
		System.out.println("Validating Generated Key mechanisms works");
		String symmetric = new String(Symm.base64.keygen());
		System.out.println(symmetric);
		Symm bsym = Symm.obtain(symmetric);
		String result = bsym.encode(encoding);
		System.out.println("\nResult:");
		System.out.println(result);
		assertEquals(encoding, bsym.decode(result));
		
		int ITER = 20000;
		System.out.println("Generating keys " + ITER + " times");
		long start = System.nanoTime();
		for(int i=0;i<ITER;++i) {
			Symm.base64.keygen();
		}
		Float ms = (System.nanoTime()-start)/1000000F;
		System.out.println("Total: " + ms + "ms");
		System.out.println("Avg:   " + ms/ITER + "ms");

		char[] manipulate = symmetric.toCharArray();
		int spot = new Random().nextInt(manipulate.length);
		manipulate[spot]|=0xFF;
		String newsymmetric = new String(manipulate);
		assertNotSame(newsymmetric, symmetric);
		try {
			bsym = Symm.obtain(newsymmetric);
			result = bsym.decode(result);
			assertEquals(encoding, result);
		} catch (IOException e) {
			// this is what we want to see if key wrong
			System.out.println(e.getMessage() + " (as expected)");
		}
	}

	private void encode(String toEncode, String expected) throws IOException {
		System.out.println("-------------------------------------------------");
		System.out.println(toEncode);
		System.out.println();
		System.out.println(expected);
		System.out.println();
		String result = Symm.base64.encode(toEncode);
		System.out.println(result);
		assertEquals(expected,result);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Symm.base64.decode(new ByteArrayInputStream(result.getBytes()), baos);
		result = baos.toString(Config.UTF_8);
		System.out.println(result);
		assertEquals(toEncode,result);
		
	}
}
