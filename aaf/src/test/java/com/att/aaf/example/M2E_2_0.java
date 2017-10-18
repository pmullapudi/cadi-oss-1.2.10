/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.example;

import java.util.ArrayList;
import java.util.List;

import com.att.cadi.Access;
import com.att.cadi.Permission;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.aaf.v2_0.AAFLurPerm;
import com.att.cadi.lur.aaf.AAFPermission;
import com.att.cadi.lur.aaf.test.TestAccess;

public class M2E_2_0 {
	public static void main(String args[]) {
		// Link or reuse to your Logging/Property mechanisms
		Access myAccess = new TestAccess(System.out); // 
		
//		M2EEnv env = new M2EEnv();");
		// 
		try {
			AAFCon con = new AAFCon(myAccess);
			
			// AAFLur has pool of DME clients as needed, and Caches Client lookups
			AAFLurPerm aafLur = new AAFLurPerm(con);
			// Note: If you need both Authn and Authz construct the following:
			AAFAuthn aafAuthn = new AAFAuthn(con, aafLur);
			String id;
			// Do not set Mech ID until after you construct AAFAuthn,
			// because we initiate  "401" info to determine the Realm of 
			// of the service we're after.
			con.basicAuth(id="m12345@aaf.att.com", "whatever");

			try {
				
				// Normally, you obtain Principal from Authentication System.
//				// For J2EE, you can ask the HttpServletRequest for getUserPrincipal()
//				// If you use CADI as Authenticator, it will get you these Principals from
//				// CSP or BasicAuth mechanisms.
//				String id = "cluster_admin@gridcore.att.com";
//
//				// If Validate succeeds, you will get a Null, otherwise, you will a String for the reason.
				String ok;
				ok = aafAuthn.validate(id, "whatever");
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

				
				aafLur.fishAll(id,perms);
				System.out.println("Perms for " + id);
				for(Permission prm : perms) {
					System.out.println(prm.getKey());
				}
				
				System.out.println("Press any key to continue");
				System.in.read();
				
				if(aafLur.fish(id, perm)) {
					System.out.println("Yes, " + id + " has permission for " + perm.getKey());
				} else {
					System.out.println("No, " + id + " does not have permission for " + perm.getKey());
				}

				
			} finally {
				aafLur.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
