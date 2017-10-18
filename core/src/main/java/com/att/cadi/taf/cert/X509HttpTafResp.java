/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.cert;

import java.io.IOException;
import java.security.Principal;

import com.att.cadi.Access;
import com.att.cadi.taf.AbsTafResp;
import com.att.cadi.taf.TafResp;

public class X509HttpTafResp extends AbsTafResp implements TafResp {
	private RESP status;
	
	public X509HttpTafResp(Access access, Principal principal, String description, RESP status) {
		super(access, principal, description);
 		this.status = status;
	}

	public RESP authenticate() throws IOException {
		return RESP.TRY_ANOTHER_TAF;
	}

	public RESP isAuthenticated() {
		return status;
	}


}
