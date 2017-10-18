/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.dos;

import java.io.IOException;

import com.att.cadi.Access;
import com.att.cadi.taf.AbsTafResp;

public class DenialOfServiceTafResp extends AbsTafResp  {
	private RESP ect;  // Homage to Arethra Franklin

	public DenialOfServiceTafResp(Access access, RESP resp, String description ) {
		super(access, null, description);
		ect = resp;
	}

	// Override base behavior of checking Principal and trying another TAF
	@Override
	public RESP isAuthenticated() {
		return ect;
	}
	

	public RESP authenticate() throws IOException {
		return ect;
	}
}
