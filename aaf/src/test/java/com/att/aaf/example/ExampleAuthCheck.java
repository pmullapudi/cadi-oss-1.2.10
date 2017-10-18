/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.example;

import com.att.cadi.Access;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AAFCon;
import com.att.cadi.lur.aaf.test.TestAccess;

public class ExampleAuthCheck {
	public static void main(String args[]) {
		// Link or reuse to your Logging mechanism
		Access myAccess = new TestAccess(System.out); // 
		
		try {
			AAFCon acon = new AAFCon(myAccess);
			AAFAuthn authn = new AAFAuthn(acon);
			long start; 
			for (int i=0;i<10;++i) {
				start = System.nanoTime();
				String err = authn.validate("", "gritty");
				if(err!=null) System.err.println(err);
				else System.out.println("I'm ok");
				
				err = authn.validate("bogus", "gritty");
				if(err!=null) System.err.println(err + " (correct error)");
				else System.out.println("I'm ok");

				System.out.println((System.nanoTime()-start)/1000000f + " ms");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}
