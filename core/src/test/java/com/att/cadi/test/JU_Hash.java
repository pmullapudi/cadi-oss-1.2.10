/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.test;

import junit.framework.Assert;

import org.junit.Test;

import com.att.cadi.CadiException;
import com.att.cadi.Hash;

public class JU_Hash {

	@Test
	public void test() throws CadiException {
		String init = "m82347@csp.att.com:kumquat8rie@#Tomatos3";
		String hashed = Hash.toHex(init.getBytes());
		System.out.println(hashed);
		byte[] ba = Hash.fromHex(hashed);
		String recon = new String(ba);
		System.out.println(recon);
		Assert.assertEquals(init, recon);
		
		init =hashed.substring(1);
		try {
			hashed = Hash.fromHex(init).toString();
			Assert.fail("Should have thrown Exception");
		} catch (CadiException e) {
			
		}
		
		init = hashed.replace('1', '~');
		try {
			hashed = Hash.fromHex(init).toString();
			Assert.fail("Should have thrown Exception");
		} catch (CadiException e) {
			
		}
	}

}
