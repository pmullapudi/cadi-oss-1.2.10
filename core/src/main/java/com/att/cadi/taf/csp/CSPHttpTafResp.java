/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.csp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.config.Config;
import com.att.cadi.taf.Redirectable;

public class CSPHttpTafResp extends CSPTafResp implements Redirectable {
	private HttpServletResponse httpResp;
	private String remediationURL;

	CSPHttpTafResp(Access access, Principal principal, String description, final HttpServletResponse resp, String remediation) {
		super(access, principal, description);
		httpResp = resp;
		remediationURL = remediation;
	}

	@Override
	public RESP isAuthenticated() {
		return principal!=null?RESP.IS_AUTHENTICATED:remediationURL==null?RESP.TRY_ANOTHER_TAF:RESP.TRY_AUTHENTICATING;
	}

	@Override
	public RESP authenticate() throws IOException {
		httpResp.sendRedirect(remediationURL);
		return RESP.HTTP_REDIRECT_INVOKED;
	}

	@Override
	public String get() {
		try {
			return "Global_Login=" + URLEncoder.encode(remediationURL,Config.UTF_8);
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

}
