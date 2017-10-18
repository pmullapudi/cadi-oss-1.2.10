/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.csp;

import java.io.IOException;
import java.security.Principal;

import com.att.cadi.Access;
import com.att.cadi.taf.AbsTafResp;
import com.att.cadi.taf.TafResp;

public class CSPTafResp extends AbsTafResp {
	CSPTafResp(Access access, Principal principal, String description) {
		super(access, principal, description);
	}

	// Cannot Authenticate without HTTP
	public RESP authenticate() throws IOException{
		return RESP.FAIL;
	}
	
	interface Creator {
		TafResp create(Access access, Principal principal, String desc, String remdialURL);
	};
	
	public static final Creator creator = new Creator() {
//		@Override
		public TafResp create(Access access, Principal principal, String desc, String remdialURL) {
			return new CSPTafResp(access, principal,desc);
		}
	};

}
