/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import aaf.xsd.Roles;

import com.att.rosetta.JaxEval;
import com.att.rosetta.Parse;
import com.att.rosetta.ParseException;
import com.att.rosetta.Parsed;

public class JaxSetDoc implements JaxEval {
	private boolean inDoc;
	private JaxEval je;
	private Roles roles;

	public JaxSetDoc() {
		inDoc = false;
		je = this;
	}

	public Roles getRoles() {
		return roles;
	}
	
	public JaxEval eval(Parsed<?> p) throws ParseException {
		switch(p.event) {
			case Parse.START_DOC:
				inDoc = true;
				break;
			case Parse.START_OBJ:
				if(inDoc)je = new JaxSetRoles(this,roles = new Roles());
				break;
			case Parse.END_DOC:
				je = null;
				break;
		}
		return je;
	}
}
