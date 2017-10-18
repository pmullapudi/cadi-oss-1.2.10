/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.cass;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.IResource;
import org.apache.cassandra.auth.Permission;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.att.cadi.aaf.cass.AAFAuthenticator;
import com.att.cadi.aaf.cass.AAFAuthorizer;

public class JU_CASS {

	private static AAFAuthenticator aa;
	private static AAFAuthorizer an;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		System.setProperty("cadi_prop_files", "/Volumes/Data/cassandra-security/2.0.10/etc/cassandra/cadi.properties");
		
		aa = new AAFAuthenticator();
		an = new AAFAuthorizer();

		aa.setup();
		an.setup(); // does nothing after aa.
		
		aa.validateConfiguration();
		
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws Exception {
			Map<String,String> creds = new HashMap<String,String>();
			creds.put("username", "m12345@aaf.localized");
			creds.put("password", "enc:lE_3rsXlG2nHNYiw8M99sEfCrYP4X0YnnUC");
			AuthenticatedUser aaf = aa.authenticate(creds);

			// Test out "aaf_default_domain
			creds.put("username", "m12345");
			aaf = aa.authenticate(creds);

			IResource resource = new IResource() {
				public String getName() {
					return "data";
				}

				public IResource getParent() {
					return null;
				}

				public boolean hasParent() {
					return false;
				}

				public boolean exists() {
					return true;
				}
				
			};
			Set<Permission> perms = an.authorize(aaf, resource);
			Assert.assertFalse(perms.isEmpty());
			
	}

}
