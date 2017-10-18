/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.http;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.Principal;

import javax.net.ssl.HttpsURLConnection;

import com.att.cadi.CadiException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfo;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cadi.principal.TGuardPrincipal;
import com.att.cadi.taf.csp.CSPPrincipal;

public class TransferSS implements SecuritySetter<HttpURLConnection> {
	private String value;
	private SecurityInfo securityInfo;
	private boolean trust;
	private SecuritySetter<HttpURLConnection> defSS;

	public TransferSS(Principal principal) throws IOException {
		if(principal instanceof CSPPrincipal) {
			value = "CSP " + ((CSPPrincipal)principal).attessec();
			trust = false;  // TODO change to true and validate
		} else if(principal instanceof TGuardPrincipal) {
			value = "TGUARD " + ((TGuardPrincipal)principal).info();
			trust = true;
		} else if(principal instanceof BasicPrincipal) {
			value = ((BasicPrincipal)principal).getAsHeader();
			trust = false;
		} else {
			trust = true;
		}
	}

	public TransferSS(Principal principal, SecurityInfo si) throws IOException {
		this(principal);
		securityInfo = si;
		this.defSS = si.defSS;
	}

	@Override
	public void setSecurity(HttpURLConnection huc) throws CadiException {
		if(value!=null) {
			if(trust) {
				if(defSS==null) throw new CadiException("Need App Credentials to send message");
				defSS.setSecurity(huc);
				huc.setRequestProperty(Config.CADI_AS_USER, value);
			} else {
				huc.setRequestProperty("Authorization", value);
			}
		}
		if(securityInfo!=null && huc instanceof HttpsURLConnection) {
			securityInfo.setSocketFactoryOn((HttpsURLConnection)huc);
		}

	}

}
