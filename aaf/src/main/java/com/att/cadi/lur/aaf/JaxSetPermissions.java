/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import aaf.xsd.Permission;
import aaf.xsd.Permissions;

import com.att.rosetta.JaxEval;
import com.att.rosetta.Parse;
import com.att.rosetta.ParseException;
import com.att.rosetta.Parsed;

/**
 * Temporary Class until full Rosetta functionality arises.
 * 
 * These do, however, utilize maximum speed.
 * 
 *
 */
public class JaxSetPermissions implements JaxEval {
	public Permissions permissions;
	private JaxEval prev;
	private JaxEval je;
	private boolean inArray = false;
	
	public JaxSetPermissions(JaxEval prev, Permissions permissions) {
		this.permissions = permissions;
		this.prev = prev;
		this.je = this;
	}
	
	public JaxEval eval(Parsed<?> p) throws ParseException {
		switch(p.event) {
			case Parse.NEXT:
			case Parse.START_OBJ:
				if(inArray) {
					Permission permisson = new Permission();
					permissions.getPermissions().add(permisson);
					je = new JaxSetPermission(this,permisson);
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
