/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.example;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import aaf.v2_0.Role;
import aaf.v2_0.Roles;

import com.att.cadi.Access;
import com.att.cadi.Permission;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.cadi.client.Future;
import com.att.cadi.lur.LocalPermission;
import com.att.cadi.lur.aaf.AAFPermission;
import com.att.inno.env.APIException;
import com.att.inno.env.impl.Log4JLogTarget;
import com.att.rosetta.env.RosettaDF;
import com.att.rosetta.env.RosettaEnv;

public class Sherry_2_0 {
	public static void main(String args[]) {
		

		// Normally, these should be set in environment.  Setting here for clarity
		Properties props = System.getProperties();
		props.setProperty("AFT_LATITUDE", "32.780140");
		props.setProperty("AFT_LONGITUDE", "-96.800451");
		props.setProperty("AFT_ENVIRONMENT", "AFTUAT");
		
//		How to access local DME2 Registered class
		props.setProperty("DME2_EP_REGISTRY_CLASS","DME2FS");
		props.setProperty("AFT_DME2_EP_REGISTRY_FS_DIR","../../authz/dme2reg");

		
		// Link or reuse to your Logging mechanism
		ScamperAccess access = new ScamperAccess(props);
 
		try {
			// Rosetta DataFactories - create as infrequently as possible... preferrably once
			RosettaDF<Roles> rolesDF = access.newDataFactory(Roles.class);
			
			
			AAFCon con = new AAFCon(access);
			
			// AAFLur has pool of DME clients as needed, and Caches Client lookups
			AAFLurPerm aafLur = new AAFLurPerm(con);
			// Note: If you need both Authn and Authz construct the following:
			AAFAuthn aafAuthn = new AAFAuthn(con, aafLur);
			String id;
			// Do not set Mech ID until after you construct AAFAuthn,
			// because we initiate  "401" info to determine the Realm of 
			// of the service we're after.
			//con.basicAuth(id="m62865@jenkins.att.com", "new2you!");
			con.basicAuth(id="m62865@jenkins.att.com","new2you!");


			try {
				
				// Normally, you obtain Principal from Authentication System.
//				// For J2EE, you can ask the HttpServletRequest for getUserPrincipal()
//				// If you use CADI as Authenticator, it will get you these Principals from
//				// CSP or BasicAuth mechanisms.
//				String id = "cluster_admin@gridcore.att.com";
//
//				// If Validate succeeds, you will get a Null, otherwise, you will a String for the reason.
				String ok;
				ok = aafAuthn.validate(id, "new2you!");
				if(ok!=null)System.out.println(ok);
				
				ok = aafAuthn.validate(id, "wrongPass");
				if(ok!=null)System.out.println(ok);

				
				ok = aafAuthn.validate(id, "new2you!");
				if(ok!=null)System.out.println(ok);

//
//				// AAF Style permissions are in the form
//				// Type, Instance, Action 
//				AAFPermission perm = new AAFPermission("com.att.grid.gridcore","dev_cluster", "ALL");
//				
				AAFPermission perm = new AAFPermission("com.att.grid.np.cass",":XSA-L3-1.2.4-QC14-allen","*");
				
				id = "mc0897@aaf.att.com";
				
				// Now you can ask the LUR (Local Representative of the User Repository about Authorization
				// With CADI, in J2EE, you can call isUserInRole("com.att.mygroup|mytype|write") on the Request Object 
				// instead of creating your own LUR
				for(int i=0;i<4;++i) {
					if(aafLur.fish(id, perm)) {
						System.out.println("Yes, " + id + " has permission for " + perm.getKey());
					} else {
						System.out.println("No, " + id + " does not have permission for " + perm.getKey());
					}
				}


				// Or you can all for all the Permissions available
				List<Permission> perms = new ArrayList<Permission>();
				
				aafLur.fishAll("jg1555@csp.att.com",perms);
				System.out.println("Perms for " + id);
				for(Permission prm : perms) {
					System.out.println(prm.getKey());
				}
				
				if(aafLur.fish("jg1555@csp.att.com",new LocalPermission("com.att.aaf.jenkins	|  mithrilcsp.sbc.com		| admin")))
					System.out.println("Yep, uhuh, have jenkins Mith etc");
				else 
					System.out.println("Nope, uhoh, have not jenkins Mith etc");

				if(aafLur.fish("jg1555@csp.att.com",new LocalPermission("com.att.aaf.jenkins|mithrilcsp.sbc.com|admin")))
					System.out.println("Yep, uhuh, have jenkins Mith etc");
				else 
					System.out.println("Nope, uhoh, have not jenkins Mith etc");

				// Make Direct API Calls
				System.out.println("Direct API reads");
				// This will make the call, but not receive... Allows you to be asyncronous
				Future<Roles> froles = con.client.read("/authz/roles/user/jg1555@csp.att.com", rolesDF);
				if(froles.get(5000 /*timeout*/)) {
					// Is true if HTTP is the accepted HTTP Code for the call, in this case, 200
					// You can depend on froles.value to be populated
					for(Role r : froles.value.getRole()) {
						System.out.println(r.getName());
					}
				} else {
					System.err.println(froles.code() + froles.body());
				}
				
				
				Future<String> fs = con.client.read("authn/basicAuth","text/plain");
				if(fs.get(5000)) {
					System.out.println("Yes, basicAuth");
				} else {
					System.out.println("No, basicAuth");
				}
			
				
				
			} finally {
				aafLur.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static class ScamperAccess extends RosettaEnv implements Access {
		private Level level;

		public ScamperAccess(Properties props) {
			super(props);
		}

		public ClassLoader classLoader() {
			return getClass().getClassLoader();
		}

		public void load(InputStream arg0) throws IOException {
			throw new IOException("Unimplemented");
		}

		public void log(Level lvl, Object... msgs) {
			if(level.compareTo(lvl)<=0) {
				switch(lvl) {
					case INIT:
						init().log(msgs);
						break;
					case AUDIT:
						audit().log(msgs);
						break;
					case DEBUG:
						debug().log(msgs);
						break;
					case ERROR:
						error().log(msgs);
					case INFO:
						info().log(msgs);
						break;
					case WARN:
						warn().log(msgs);
						break;
				}
			}
		}

		public void log(Exception e, Object... msgs) {
			error().log(e,msgs);
		}

		public void setLogLevel(Level level) {
			this.level = level;
		}

		// Not overriden.  Do however you logging works
		public void setLog4JNames(String service, String audit, String init) throws APIException {
			super.fatal = new Log4JLogTarget(service,org.apache.log4j.Level.FATAL);
			super.error = new Log4JLogTarget(service,org.apache.log4j.Level.ERROR);
			super.warn = new Log4JLogTarget(service,org.apache.log4j.Level.WARN);
			super.audit = new Log4JLogTarget(audit,org.apache.log4j.Level.WARN);
			super.init = new Log4JLogTarget(init,org.apache.log4j.Level.WARN);
			super.info = new Log4JLogTarget(service,org.apache.log4j.Level.INFO);
			super.debug = new Log4JLogTarget(service,org.apache.log4j.Level.DEBUG);
			super.trace = new Log4JLogTarget(service,org.apache.log4j.Level.TRACE);
		}

		public String decrypt(String encrypted, boolean anytext) throws IOException {
			return encrypted;
		}


	}

}
