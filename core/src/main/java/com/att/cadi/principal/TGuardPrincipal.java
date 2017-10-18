/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.principal;

import java.security.Principal;

public class TGuardPrincipal implements Principal {

	private String name, tresp;

	public TGuardPrincipal(String tresp) {
		this.tresp=tresp;
	}

	/**
	 * TODO Need to figure out what Organizations TGuard entities should be part of.  
	 * 
	 */
	public String getName() {
		if(name==null) {
			String temp = get("iv-user");
			if(temp==null)return null;
			StringBuilder sb = new StringBuilder();
			int at = temp.indexOf('@');
			if(at<0) {
				sb.append(temp);
			} else {
				sb.append(temp.substring(0, at));
			}
			if(temp.endsWith("@uverse.com"))sb.append("@uverse.tguard.att.com");
			else if(temp.endsWith("@att.com"))sb.append("@com.tguard.att.com");
			else if(temp.endsWith("@att.net"))sb.append("@net.tguard.att.com");
			else sb.append("@tguard.att.com");
			name = sb.toString();
		}
		return name;
	}

	/**
	 * Get a value from a named TGuard Property
	 * 
	 * TGuard response info is very dynamic.  They can add new properties at any time, so we dare not code field names for these values.
	 * @param key
	 * @return
	 */
	public String get(String key) {
		if(key==null)return null;
		int idx=0,equal=0,amp=0;
		while(idx>=0 && (equal = tresp.indexOf('=',idx))>=0) {
			amp = tresp.indexOf('&',equal);
			if(key.regionMatches(0, tresp, idx, equal-idx)) {
				return amp>=0?tresp.substring(equal+1, amp):tresp.substring(equal+1); 
			}
			idx=amp+(amp>0?1:0);
		}
		return null;
	}

	public String info() {
		return tresp;
	}
}
