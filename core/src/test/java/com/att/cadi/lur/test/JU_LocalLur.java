/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;

import com.att.cadi.CredVal.Type;
import com.att.cadi.Lur;
import com.att.cadi.Permission;
import com.att.cadi.Symm;
import com.att.cadi.config.UsersDump;
import com.att.cadi.lur.LocalLur;
import com.att.cadi.lur.LocalPermission;

public class JU_LocalLur {

	@Test
	public void test() throws IOException {
		Symm symmetric = Symm.baseCrypt().obtain();
		LocalLur up;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		baos.write(Symm.ENC.getBytes());
		symmetric.enpass("whatever", baos);
		TestAccess ta = new TestAccess(symmetric);
		Lur ml = up = new LocalLur(ta,"jg1555:groupA,groupB","admin:jg1555,kj2534;suser:lm1533,yu2344,m1234%"+baos.toString());
		
		Permission admin = new LocalPermission("admin");
		Permission suser = new LocalPermission("suser");
		
		// Check User fish
		assertTrue(ml.fish(new JUPrincipal("jg1555"),admin));
		assertTrue(ml.fish(new JUPrincipal("kj2534"),admin));
		assertFalse(ml.fish(new JUPrincipal("nm5333"),admin));
		assertTrue(ml.fish(new JUPrincipal("lm1533"),suser));
		assertTrue(ml.fish(new JUPrincipal("yu2344"),suser));
		assertFalse(ml.fish(new JUPrincipal("jg1555"),suser));
		
		
		// Check validate password
		assertTrue(up.validate("m1234",Type.PASSWORD, "whatever".getBytes()));
		assertFalse(up.validate("m1234",Type.PASSWORD, "badPass".getBytes()));
		
		// Check fishAll
		Set<String> set = new TreeSet<String>();
		List<Permission> perms = new ArrayList<Permission>();
		ml.fishAll(new JUPrincipal("jg1555"), perms);
		for(Permission p : perms) {
			set.add(p.getKey());
		}
		assertEquals("[admin, groupA, groupB]",set.toString());
		UsersDump.write(System.out, up);
		System.out.flush();
		
	}
	
	// Simplistic Principal for testing purposes
	private static class JUPrincipal implements Principal {
		private String name;
		public JUPrincipal(String name) {
			this.name = name;
		}
//		@Override
		public String getName() {
			return name;
		}
	}

}
