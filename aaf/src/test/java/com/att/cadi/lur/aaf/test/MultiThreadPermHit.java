/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf.test;

import java.util.ArrayList;
import java.util.List;

import com.att.cadi.Access;
import com.att.cadi.Permission;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.cadi.config.Config;
import com.att.cadi.lur.aaf.AAFPermission;

public class MultiThreadPermHit {
	public static void main(String args[]) {
		// Link or reuse to your Logging mechanism
		Access myAccess = new TestAccess(System.out); // 
		
		// 
		try {
			AAFCon con = new AAFCon(myAccess);
			
			// AAFLur has pool of DME clients as needed, and Caches Client lookups
			final AAFLurPerm aafLur = new AAFLurPerm(con);
			aafLur.setDebug("m12345@aaf.att.com");
			// Note: If you need both Authn and Authz construct the following:
			AAFAuthn aafAuthn = new AAFAuthn(con, aafLur);
			
			// Do not set Mech ID until after you construct AAFAuthn,
			// because we initiate  "401" info to determine the Realm of 
			// of the service we're after.
			final String id = myAccess.getProperty(Config.AAF_MECHID,null);
			final String pass = myAccess.decrypt(myAccess.getProperty(Config.AAF_MECHPASS,null),false);
			if(id!=null && pass!=null) {
				con.basicAuth(id, pass);
				try {
					
					// Normally, you obtain Principal from Authentication System.
	//				// For J2EE, you can ask the HttpServletRequest for getUserPrincipal()
	//				// If you use CADI as Authenticator, it will get you these Principals from
	//				// CSP or BasicAuth mechanisms.
	//				String id = "cluster_admin@gridcore.att.com";
	//
	//				// If Validate succeeds, you will get a Null, otherwise, you will a String for the reason.
					String ok;
					ok = aafAuthn.validate(id, pass);
					if(ok!=null)System.out.println(ok);
					
					ok = aafAuthn.validate(id, "wrongPass");
					if(ok!=null)System.out.println(ok);
	
					final AAFPermission perm = new AAFPermission("com.att.aaf.access","*","*");
					
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
	
					
					aafLur.fishAll(id,perms);
					System.out.println("Perms for " + id);
					for(Permission prm : perms) {
						System.out.println(prm.getKey());
					}
					
					System.out.println("Press any key to continue");
					System.in.read();
					
					for(int j=0;j<5;++j) {
						new Thread(new Runnable() {
							@Override
							public void run() {
								for(int i=0;i<20;++i) {
									if(aafLur.fish(id, perm)) {
										System.out.println("Yes, " + id + " has permission for " + perm.getKey());
									} else {
										System.out.println("No, " + id + " does not have permission for " + perm.getKey());
									}
								}
							}
						}).start();
					}
	
					
				} finally {
					aafLur.destroy();
				}
			} else { // checked on IDs
				System.err.println(Config.AAF_MECHID + " and/or " + Config.AAF_MECHPASS + " are not set.");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
