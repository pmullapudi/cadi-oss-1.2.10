/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import com.att.cadi.Permission;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.cadi.aaf.v2_0.AAFTaf;
import com.att.cadi.config.Config;
import com.att.cadi.principal.CachedBasicPrincipal;

public class JU_JMeter {
	private static AAFCon aaf;
	private static AAFAuthn aafAuthn;
	private static AAFLurPerm aafLur;
	private static ArrayList<Principal> perfIDs;
	
	private static AAFTaf aafTaf;

	@BeforeClass
	public static void before() throws Exception {
		if(aafLur==null) {
			Properties props = System.getProperties();
			props.setProperty("AFT_LATITUDE", "32.780140");
			props.setProperty("AFT_LONGITUDE", "-96.800451");
			props.setProperty("DME2_EP_REGISTRY_CLASS","DME2FS");
			props.setProperty("AFT_DME2_EP_REGISTRY_FS_DIR","/Volumes/Data/src/authz/dme2reg");
			props.setProperty("AFT_ENVIRONMENT", "AFTUAT");
			props.setProperty("SCLD_PLATFORM", "NON-PROD");
			props.setProperty(Config.AAF_URL,"https://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=2.0/envContext=DEV/routeOffer=BAU_SE");
			props.setProperty(Config.AAF_READ_TIMEOUT, "2000");
			int timeToLive = 3000;
			props.setProperty(Config.AAF_CLEAN_INTERVAL, Integer.toString(timeToLive));
			props.setProperty(Config.AAF_HIGH_COUNT, "4");

			String aafPerfIDs = props.getProperty("AAF_PERF_IDS");
			perfIDs = new ArrayList<Principal>();
			File perfFile = null;
			if(aafPerfIDs!=null) {
				perfFile = new File(aafPerfIDs);
			}

			aaf = new AAFCon(new TestAccess(System.out));
			aafTaf = new AAFTaf(aaf,false);
			aafLur = new AAFLurPerm(aaf, aafTaf);
			aafAuthn = new AAFAuthn(aaf,aafLur);
			aaf.basicAuth("testid@aaf.att.com", "whatever");

			if(perfFile==null||!perfFile.exists()) {
				perfIDs.add(new CachedBasicPrincipal(aafTaf, 
						"Basic dGVzdGlkOndoYXRldmVy", 
						"aaf.att.com",timeToLive));
				perfIDs.add(new Princ("jg1555@aaf.att.com")); // Example of Local ID, which isn't looked up
			} else {
				BufferedReader ir = new BufferedReader(new FileReader(perfFile));
				try {
					String line;
					while((line = ir.readLine())!=null) {
						if((line=line.trim()).length()>0)
							perfIDs.add(new Princ(line));
					}
				} finally {
					ir.close();
				}
			}
			Assert.assertNotNull(aafLur);
		}
	}

	private static class Princ implements Principal {
		private String name;
		public Princ(String name) {
			this.name = name;
		}
		public String getName() {
			return name;
		}
		
	};
	
	private static int index = -1;
	
	private synchronized Principal getIndex() {
		if(perfIDs.size()<=++index)index=0;
		return perfIDs.get(index);
	}
	@Test
	public void test() {
		try {
				aafAuthn.validate("testid@aaf.att.com", "whatever");
				List<Permission> perms = new ArrayList<Permission>();
				aafLur.fishAll(getIndex(), perms);
//				Assert.assertFalse(perms.isEmpty());
//				for(Permission p : perms) {
//					//access.log(Access.Level.AUDIT, p.permType());
//				}
		} catch (Exception e) {
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			Assert.assertFalse(sw.toString(),true);
		}
	}

}
