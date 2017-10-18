/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import com.att.cadi.lur.aaf.PermEval;

public class JU_PermEval {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		
		// TRUE
		assertTrue(PermEval.evalAction("fred","fred"));
		assertTrue(PermEval.evalAction("fred,wilma","fred"));
		assertTrue(PermEval.evalAction("barney,betty,fred,wilma","fred"));
		assertTrue(PermEval.evalAction("*","fred"));
		
		assertTrue(PermEval.evalInstance("fred","fred"));
		assertTrue(PermEval.evalInstance("fred,wilma","fred"));
		assertTrue(PermEval.evalInstance("barney,betty,fred,wilma","fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		
		assertTrue(PermEval.evalInstance(":fred:fred",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:fred,wilma",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:barney,betty,fred,wilma",":fred:fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		assertTrue(PermEval.evalInstance(":*:fred",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:*",":fred:fred"));
		assertTrue(PermEval.evalInstance(":fred:fred",":!f.*:fred"));
		assertTrue(PermEval.evalInstance(":fred:fred",":fred:!f.*"));
		
		/// FALSE
		assertFalse(PermEval.evalInstance("fred","wilma"));
		assertFalse(PermEval.evalInstance("fred,barney,betty","wilma"));
		assertFalse(PermEval.evalInstance(":fred:fred",":fred:wilma"));
		assertFalse(PermEval.evalInstance(":fred:fred",":wilma:fred"));
		assertFalse(PermEval.evalInstance(":fred:fred",":wilma:!f.*"));
		assertFalse(PermEval.evalInstance(":fred:fred",":!f.*:wilma"));
		assertFalse(PermEval.evalInstance(":fred:fred",":!w.*:!f.*"));
		assertFalse(PermEval.evalInstance(":fred:fred",":!f.*:!w.*"));

		assertFalse(PermEval.evalInstance(":fred:fred",":fred:!x.*"));

		// MSO Tests 12/3/2015
		assertFalse(PermEval.evalInstance("/v1/services/features/*","/v1/services/features"));
		assertFalse(PermEval.evalInstance(":v1:services:features:*",":v1:services:features"));
		assertTrue(PermEval.evalInstance("/v1/services/features/*","/v1/services/features/api1"));
		assertTrue(PermEval.evalInstance(":v1:services:features:*",":v1:services:features:api2"));
		// MSO - Xue Gao
		assertTrue(PermEval.evalInstance(":v1:requests:*",":v1:requests:test0-service"));   


		
		// Same tests, with Slashes
		assertTrue(PermEval.evalInstance("/fred/fred","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/fred,wilma","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/barney,betty,fred,wilma","/fred/fred"));
		assertTrue(PermEval.evalInstance("*","fred"));
		assertTrue(PermEval.evalInstance("/*/fred","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/*","/fred/fred"));
		assertTrue(PermEval.evalInstance("/fred/fred","/!f.*/fred"));
		assertTrue(PermEval.evalInstance("/fred/fred","/fred/!f.*"));
		
		/// FALSE
		assertFalse(PermEval.evalInstance("fred","wilma"));
		assertFalse(PermEval.evalInstance("fred,barney,betty","wilma"));
		assertFalse(PermEval.evalInstance("/fred/fred","/fred/wilma"));
		assertFalse(PermEval.evalInstance("/fred/fred","/wilma/fred"));
		assertFalse(PermEval.evalInstance("/fred/fred","/wilma/!f.*"));
		assertFalse(PermEval.evalInstance("/fred/fred","/!f.*/wilma"));
		assertFalse(PermEval.evalInstance("/fred/fred","/!w.*/!f.*"));
		assertFalse(PermEval.evalInstance("/fred/fred","/!f.*/!w.*"));

		assertFalse(PermEval.evalInstance("/fred/fred","/fred/!x.*"));
		
	}

}
