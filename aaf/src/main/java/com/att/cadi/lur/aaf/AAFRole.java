/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import aaf.xsd.Role;

import com.att.cadi.Permission;

/**
 * A Class that understands the AAF format of Permission (name/type/action)
 * 
 * @author jg1555
 *
 */
public class AAFRole implements Permission {
	public final Role role;

	public AAFRole(Role role) {
		this.role = role;
	}

	public String getKey() {
		return role.getRoleName();
	}
	
	public boolean match(Permission p) {
		String name = role.getRoleName();
		if(name==null)return false;
		return name.equals(p.getKey());
	}
	
	/* (non-Javadoc)
	 * @see com.att.cadi.Permission#permType()
	 */
	public String permType() {
		return "AAFRole";
	}

}
