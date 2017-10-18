/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf.test;

import static org.junit.Assert.assertEquals;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.BeforeClass;
import org.junit.Test;

import com.att.cadi.CadiException;
import com.att.cadi.Lur;
import com.att.cadi.Permission;
import com.att.cadi.lur.ConfigPrincipal;
import com.att.cadi.lur.LocalPermission;
import com.att.cadi.lur.aaf.AAFLurPerm1_0;
import com.att.cadi.lur.aaf.AAFLurRole1_0;

public class JU_Lur1_0Call {
	private static TestAccess ta;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		ta = new TestAccess(System.out);
	}

	@Test
	public void test() throws CadiException {
		AAFLurPerm1_0 aafLur = new AAFLurPerm1_0 (
				ta,
//				"http://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=2.0/envContext=DEV/routeOffer=BAU_SE",
				"http://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=1.0.0/envContext=UAT/routeOffer=BAU_SE",
				"m12345", "m12345pass", 50000, // dme Time
				// 5*60000); // 5 minutes User Expiration
				50000, // 5 seconds after Expiration
				200); // High Count of items.. These do not take much memory


		Principal pri = new ConfigPrincipal("jg1555","");

		List<Permission> perms = new ArrayList<Permission>();
		aafLur.fishAll(pri, perms);
		for(Permission p : perms) {
			System.out.println(p);
		}

		for (int i = 0; i < 10; ++i) {
			print(aafLur, pri, new LocalPermission("component|svn:svnclient|*"), true);
			print(aafLur, pri, new LocalPermission("com.att.cadi.service|kumquat|write"),false);
		}

		print(aafLur, pri, new LocalPermission("bogus"),false);

		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("All Done");
	}

	
	public void testRole() throws CadiException {
		TestAccess ta = new TestAccess(System.out);
		AAFLurRole1_0 aafLur = new AAFLurRole1_0(
				ta,
				"http://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=1.0.0/envContext=UAT/routeOffer=BAU_SE",
//				"http://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=1.0.0/envContext=DEV/routeOzffer=D1",
				"m12345", "m12345pass", 50000, // dme Time
				// 5*60000); // 5 minutes User Expiration
				5000, // 5 seconds after Expiration
				200); // High Count of items.. These do not take much memory

		Principal pri = new ConfigPrincipal("jg1555","");
		for (int i = 0; i < 10; ++i) {
			print(aafLur, pri, new LocalPermission("com.att.cadi"),true);
			print(aafLur, pri, new LocalPermission("global"),true);
			print(aafLur, pri, new LocalPermission("kumquat"),false);
		}

		print(aafLur, pri, new LocalPermission("bogus"),false);

		for (int i = 0; i < 10; ++i)
			print(aafLur, pri, new LocalPermission("supergroup"),false);

		System.out.println("All Done");
	}


	private void print(Lur aafLur, Principal pri, Permission perm, boolean shouldBe)
			throws CadiException {
		long start = System.nanoTime();
	
		// The Call
		boolean ok = aafLur.fish(pri, perm);
	
		assertEquals(shouldBe,ok);
		float ms = (System.nanoTime() - start) / 1000000f;
		if (ok) {
			System.out.println("Yes, part of " + perm.getKey() + " (" + ms
					+ "ms)");
		} else {
			System.out.println("No, not part of " + perm.getKey() + " (" + ms
					+ "ms)");
		}
	}

//	public static void main(String args[]) {
//		String userId = args[0];
//		String role = args[1];
//		String dme2Url = args[2];
//
//		try {
//			setUpBeforeClass();
//			TestAccess ta = new TestAccess();
//			AbsAAFLur<AAFPermission> aafLur = new AAFLurPerm1_0(ta, dme2Url, "CustomAuthz", "test",
//					5000, // dme Time
//					// 5*60000); // 5 minutes User Expiration
//					5000, // 5 seconds after Expiration
//					200); // High Count of items.. These do not take much memory
//
//			Principal pri = new BasicPrincipal(userId);
//			LocalPermission perm = new LocalPermission(role);
//			boolean ok = aafLur.fish(pri, perm);
//
//			if (ok) {
//				System.out.println(userId + " has AAF role " + role);
//			} else {
//				System.out.println(userId + " does not have AAF role " + role);
//			}
//			System.exit(0);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//
//	}
}
