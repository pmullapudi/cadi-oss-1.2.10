/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.localhost;

import java.security.Principal;

import com.att.cadi.Access;
import com.att.cadi.taf.TafResp;

public class LocalhostTafResp implements TafResp {
	private RESP action;
	private String description;
	private final static Principal principal = new Principal() {
		private String name = System.getProperty("user.name")+"@localhost";
//		@Override
		public String getName() {
			return name;
		}
	};

	private Access access;
	
	public LocalhostTafResp(Access access, RESP state, String desc) {
		action = state;
		description = desc;
		this.access = access;
	}
	
//	@Override
	public boolean isValid() {
		return action == RESP.IS_AUTHENTICATED;
	}

//	@Override
	public String desc() {
		return description;
	}

//	@Override
	public RESP authenticate() {
		return action;
	}
	
	public RESP isAuthenticated() {
		return action;
	}

//	@Override
	public Principal getPrincipal() {
		return principal;
	}

	public Access getAccess() {
		return access;
	}

	public boolean isFailedAttempt() {
		return false;
	}

}
