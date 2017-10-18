/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import aaf.xsd.Permission;

/**
 * AAF 1.0, unfortunately, has a bad return structure.  Here, we'll 
 * split it out correctly.
 * 
 * @author jg1555
 *
 */
public class AAF_1_0_Permission extends AAFPermission {
	public final Permission perm;
	public AAF_1_0_Permission(Permission p) {
		super(); // start uninitialized
		perm = p;
		String split[] = p.getPermission().split("\\|",4);
		// Note: No Break; statements on purpose.
		switch(split.length) {
			case 0:
				type=instance=action="";
				break;
			case 1:
				type=split[0];
				instance=action="";
				break;
			case 2:
				type=split[0];
				action=split[1];
				instance="";
				break;
			default:
				type=split[0];
				action=split[1];
				instance=split[2];
		}
		key = type + '|' + instance + '|' + action;
	}

}
