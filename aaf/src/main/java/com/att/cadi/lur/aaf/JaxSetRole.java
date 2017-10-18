/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import aaf.xsd.Role;

import com.att.rosetta.JaxEval;
import com.att.rosetta.JaxSet;
import com.att.rosetta.JaxSet.Setter;
import com.att.rosetta.Parse;
import com.att.rosetta.ParseException;
import com.att.rosetta.Parsed;

public class JaxSetRole implements JaxEval {
	private Role role;
	private JaxSet<Object> jaxSet;
	private JaxEval prev;
	
	public JaxSetRole(JaxEval prev, Role role) {
		this.role = role;
		jaxSet = JaxSet.get(Role.class);
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
						s.set(role, p.sb.toString());
					}
				}
				break;
			}
		return rv;
		}
}

