/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf;

import java.io.IOException;
import java.security.Principal;

import com.att.cadi.Access;

/**
 * A Punt Resp to make it fast and easy for a Taf to respond that it cannot handle a particular kind of
 * request.  It is always the same object, so there is no cost for memory, etc.
 *
 */
public class PuntTafResp implements TafResp {
	private PuntTafResp(){}
	
	private static TafResp singleton = new PuntTafResp();
	
	public static TafResp singleton() {
		return singleton;
	}
	
	public boolean isValid() {
		return false;
	}
	
	public RESP isAuthenticated() {
		return RESP.TRY_ANOTHER_TAF;
	}
	
	public String desc() {
		return "This Taf can or will not handle this authentication";
	}
	
	public RESP authenticate() throws IOException {
		return RESP.TRY_ANOTHER_TAF;
	}

	public Principal getPrincipal() {
		return null;
	}

	public Access getAccess() {
		return NullTafResp.singleton().getAccess();
	}

	public boolean isFailedAttempt() {
		return false;
	}
}
