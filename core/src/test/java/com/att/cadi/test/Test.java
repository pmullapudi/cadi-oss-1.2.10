/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.test;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.att.cadi.taf.csp.CSPTaf;

/**
 * A Class to run on command line to determine suitability of environment for certain TAFs.
 * 
 * For instance, CSP supports services only in certain domains, and while dynamic host
 * lookups on the machine work in most cases, sometimes, names and IPs are unexpected (and
 * invalid) for CSP because of multiple NetworkInterfaces, etc
 * 
 * @author jg1555
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			System.out.println("CADI/TAF test");
			
			String hostName = InetAddress.getLocalHost().getCanonicalHostName();
			
			System.out.println("  Your automatic hostname is reported as \"" + hostName + "\"\n");
			String[] two;

			for(String str : args) {
				two = str.split("=");
				if(two.length==2) {
					if("hostname".equals(two[0])) {
						hostName = two[1];
						System.out.println("  You have overlaid the automatic hostname with \"" + hostName + "\"\n");
					}
				}
			}
			if(hostName.endsWith("vpn.cingular.net"))
				System.out.println("  This service appears to be an AT&T VPN address. These VPNs are typically\n" +
						"    (and appropriately) firewalled against incoming traffic, and likely cannot be accessed.\n" +
						"    For best results, choose a machine that is not firewalled on the ports you choose.\n");
			System.out.println(CSPTaf.domainSupportedCSP(hostName, "  "));
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

	}

}
