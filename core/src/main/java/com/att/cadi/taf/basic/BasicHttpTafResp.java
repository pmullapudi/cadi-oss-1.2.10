/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.basic;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.taf.AbsTafResp;
import com.att.cadi.taf.TafResp;

public class BasicHttpTafResp extends AbsTafResp implements TafResp {
	private HttpServletResponse httpResp;
	private String realm;
	private RESP status;
	private final boolean wasFailed;
	
	public BasicHttpTafResp(Access access, Principal principal, String description, RESP status, HttpServletResponse resp, String realm, boolean wasFailed) {
		super(access,principal, description);
		httpResp = resp;
		this.realm = realm;
		this.status = status;
		this.wasFailed = wasFailed;
	}

	public RESP authenticate() throws IOException {
		httpResp.setStatus(401); // Unauthorized	
		httpResp.setHeader("WWW-Authenticate", "Basic realm=\""+realm+'"');
		return RESP.HTTP_REDIRECT_INVOKED;
	}

	public RESP isAuthenticated() {
		return status;
	}

	public boolean isFailedAttempt() {
		return wasFailed;
	}


}
