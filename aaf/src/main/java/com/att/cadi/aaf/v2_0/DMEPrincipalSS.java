/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.aaf.v2_0;

import java.io.IOException;
import java.security.Principal;

import com.att.aft.dme2.api.DME2Client;
import com.att.cadi.CadiException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.lur.ConfigPrincipal;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cadi.taf.csp.CSPPrincipal;

public class DMEPrincipalSS implements SecuritySetter<DME2Client> {
	private static final String AUTHORIZATION = "Authorization";
	private final Principal p;
//	private final SecuritySetter<DME2Client> serverSS;

	public DMEPrincipalSS(Principal principal) {
		p = principal;
//		serverSS = null;
	}

//	public DMEPrincipalSS(Principal principal, SecuritySetter<DME2Client> serverSS) {
//		p = principal;
//		this.serverSS = serverSS;
//	}
//	
	public void setSecurity(DME2Client client) throws CadiException {
		try {
			if(p instanceof BasicPrincipal) {
				client.addHeader(AUTHORIZATION, ((BasicPrincipal)p).getAsHeader());
			} else if(p instanceof ConfigPrincipal) {
				// DME2 Doesn't reset credentials originally passed correctly
				// client.setCredentials(cp.getName(), new String(cp.getCred()));
				client.addHeader(AUTHORIZATION, ((ConfigPrincipal)p).getAsBasicAuthHeader());
			} else if(p instanceof CSPPrincipal) {
//				if(serverSS==null) {
//					throw new APIException("Cannot talk to AAF with CSP Credential without AAF Mech Credentials");
//				} else {
					// Use Server Credentials, but add Client's
					//serverSS.setSecurity(client);
					client.addHeader(AUTHORIZATION,((CSPPrincipal)p).getAsCspAuthHeader());
//				}
			} else  {
				throw new CadiException("Unsupported Principal Type: " + p.getClass().getName()); 
			}
		} catch(CadiException e) {
			throw e;
		} catch(IOException e) {
			throw new CadiException(e);
		}
	}

}
