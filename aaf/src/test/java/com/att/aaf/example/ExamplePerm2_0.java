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

public class ExamplePerm2_0 {
	public static void main(String args[]) {
		// Link or reuse to your Logging mechanism
		Access myAccess = new TestAccess(System.out);  
		
		// 
		try {
			AAFCon con = new AAFCon(myAccess);
			
			// AAFLur has pool of DME clients as needed, and Caches Client lookups
			AAFLurPerm aafLur = new AAFLurPerm(con);
			// Note: If you need both Authn and Authz construct the following:
			AAFAuthn aafAuthn = new AAFAuthn(con, aafLur);

			// Do not set Mech ID until after you construct AAFAuthn,
			// because we initiate  "401" info to determine the Realm of 
			// of the service we're after.
			con.basicAuth("mc0897@aaf.att.com", "XXXXXX");

			try {
				
				// Normally, you obtain Principal from Authentication System.
				// For J2EE, you can ask the HttpServletRequest for getUserPrincipal()
				// If you use CADI as Authenticator, it will get you these Principals from
				// CSP or BasicAuth mechanisms.
				String id = "mc0897@aaf.att.com"; //"cluster_admin@gridcore.att.com";

				// If Validate succeeds, you will get a Null, otherwise, you will a String for the reason.
				String ok = aafAuthn.validate(id, "XXXXXX");
				if(ok!=null)System.out.println(ok);
				
				ok = aafAuthn.validate(id, "wrongPass");
				if(ok!=null)System.out.println(ok);


				// AAF Style permissions are in the form
				// Type, Instance, Action 
				AAFPermission perm = new AAFPermission("com.att.grid.core.coh",":dev_cluster", "WRITE");
				
				// Now you can ask the LUR (Local Representative of the User Repository about Authorization
				// With CADI, in J2EE, you can call isUserInRole("com.att.mygroup|mytype|write") on the Request Object 
				// instead of creating your own LUR
				System.out.println("Does " + id + " have " + perm);
				if(aafLur.fish(id, perm)) {
					System.out.println("Yes, you have permission");
				} else {
					System.out.println("No, you don't have permission");
				}

				System.out.println("Does Bogus have " + perm);
				if(aafLur.fish("Bogus", perm)) {
					System.out.println("Yes, you have permission");
				} else {
					System.out.println("No, you don't have permission");
				}

				// Or you can all for all the Permissions available
				List<Permission> perms = new ArrayList<Permission>();
				
				aafLur.fishAll(id,perms);
				for(Permission prm : perms) {
					System.out.println(prm.getKey());
				}
				
				// It might be helpful in some cases to clear the User's identity from the Cache
				aafLur.remove(id);
			} finally {
				aafLur.destroy();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
