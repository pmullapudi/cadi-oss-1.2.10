/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import aaf.xsd.Role;
import aaf.xsd.Roles;

import com.att.rosetta.JaxEval;
import com.att.rosetta.Parse;
import com.att.rosetta.ParseException;
import com.att.rosetta.Parsed;

/**
 * Temporary Class until full Rosetta functionality arises.
 * 
 * These do, however, utilize maximum speed.
 * @author jg1555
 *
 */
public class JaxSetRoles implements JaxEval {
	public Roles roles;
	private JaxEval prev;
	private JaxEval je;
	private boolean inArray = false;
	
	public JaxSetRoles(JaxEval prev, Roles roles) {
		this.roles = roles;
		this.prev = prev;
		this.je = this;
	}
	
	public JaxEval eval(Parsed<?> p) throws ParseException {
		switch(p.event) {
			case Parse.NEXT:
			case Parse.START_OBJ:
				if(inArray) {
					Role role = new Role();
					roles.getRoles().add(role);
					je = new JaxSetRole(this,role);
				}
				break;
			case Parse.END_OBJ:
				if(!inArray)je = prev;
				break;
			case Parse.START_ARRAY:
				inArray = true;
				break;
			case Parse.END_ARRAY:
				inArray = false;
				break;
		}
		return je;
	}

}
