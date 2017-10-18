/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.example;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import com.att.cadi.Access;
import com.att.cadi.CadiException;
import com.att.cadi.Permission;
import com.att.cadi.lur.ConfigPrincipal;
import com.att.cadi.lur.LocalPermission;
import com.att.cadi.lur.aaf.AAFLurRole1_0;
import com.att.cadi.lur.aaf.test.TestAccess;

public class ExampleRole {
	public static void main(String args[]) {
		// Link or reuse to your Logging mechanism
		Access myAccess = new TestAccess(System.out); // 

		try {
			// AAFLur has pool of DME clients as needed, and Caches Client lookups
			AAFLurRole1_0 aafLur = new AAFLurRole1_0(myAccess, 
					"http://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=1.0.0/envContext=UAT/routeOffer=BAU_SE",
					"m12345", 			// ID to AAF System
					"m12345pass",       // Password for AAF System
					10000, 				// DME2 Timeout, here, we set to 10 seconds maximum
					5*60000, 			// 5 minutes for found items to live in cache
					400 				// Maximum number of items in Cache
					);
		
			try {
				// Normally, you obtain Principal from Authentication System.
				// For J2EE, you can ask the HttpServletRequest for getUserPrincipal()
				// If you use CADI as Authenticator, it will get you these Principals from
				// CSP or BasicAuth mechanisms.
				Principal p = new ConfigPrincipal("xy1234","whatever");
				
				// AAF Style permissions are in the form
				// Resource Name, Resource Type, Action 
				LocalPermission perm = new LocalPermission("myRole");
				
				// Now you can ask the LUR (Local Representative of the User Repository about Authorization
				// With CADI, in J2EE, you can call isUserInRole("com.att.mygroup|mytype|write") on the Request Object 
				// instead of creating your own LUR
				if(aafLur.fish(p, perm)) {
					System.out.println("Yes, you have permission");
				} else {
					System.out.println("No, you don't have permission");
				}
				
				// Or you can all for all the Permissions available
				List<Permission> perms = new ArrayList<Permission>();
				
				aafLur.fishAll(p,perms);
				for(Permission prm : perms) {
					System.out.println(prm.getKey());
				}
			} finally {
				aafLur.destroy();
			}
		} catch (CadiException e) {
			e.printStackTrace();
		}

	}
}
