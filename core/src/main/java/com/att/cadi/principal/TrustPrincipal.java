/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.principal;

import java.security.Principal;

import com.att.cadi.UserChain;

public class TrustPrincipal implements Principal, UserChain {
	private final String name;
	private final Principal original;
	private String userChain;
	
	public TrustPrincipal(Principal actual, String asName) {
		this.original = actual;
		name = asName;
		if(actual instanceof UserChain) {
			UserChain uc = (UserChain)actual;
			userChain = uc.userChain();
		} else {
			userChain = "";
		}
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String userChain() {
		return userChain;
	}
	
	public Principal original() {
		return original;
	}
	
}
