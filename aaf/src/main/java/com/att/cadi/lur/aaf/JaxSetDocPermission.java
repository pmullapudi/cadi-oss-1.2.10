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

public class JaxSetDocPermission implements JaxEval {
	private boolean inDoc;
	private JaxEval je;
	private Permissions permissions;

	public JaxSetDocPermission() {
		inDoc = false;
		je = this;
	}

	public Permissions getPermissions() {
		return permissions;
	}
	
	public JaxEval eval(Parsed<?> p) throws ParseException {
		switch(p.event) {
			case Parse.START_DOC:
				inDoc = true;
				break;
			case Parse.START_OBJ:
				if(inDoc)je = new JaxSetPermissions(this,permissions = new Permissions());
				break;
			case Parse.END_DOC:
				je = null;
				break;
		}
		return je;
	}
	
	public void parsePermissionStr() {
		for(Permission perm : permissions.getPermissions()) {
			String[] permTks = perm.getPermission().split("\\|");
			if (permTks.length > 3) {
				perm.setResourceType(permTks[0]);
				perm.setAction(permTks[1]);
				perm.setResourceInstance(permTks[2]);
				perm.setRoleName(permTks[3]);
			}			
		}
		
	}
}
