/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import aaf.xsd.Permission;

import com.att.rosetta.JaxEval;
import com.att.rosetta.JaxSet;
import com.att.rosetta.JaxSet.Setter;
import com.att.rosetta.Parse;
import com.att.rosetta.ParseException;
import com.att.rosetta.Parsed;

public class JaxSetPermission implements JaxEval {
	private Permission permission;
	private JaxSet<Object> jaxSet;
	private JaxEval prev;
	
	public JaxSetPermission(JaxEval prev, Permission permission) {
		this.permission = permission;
		jaxSet = JaxSet.get(Permission.class);
		this.prev = prev;
	}

	public JaxEval eval(Parsed<?> p) throws ParseException {
		JaxEval rv = this;
		switch(p.event) {
			case Parse.END_OBJ:
				rv = prev;
				// Pass through on purpose break;
			case Parse.NEXT:
				if(p.sb.length()>0) {
					Setter<Object> s = jaxSet.get(p.name);
					if(s!=null) {
						s.set(permission, p.sb.toString());
					}
				}
				break;
			}
		return rv;
	}
	
}

