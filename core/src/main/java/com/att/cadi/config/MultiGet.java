/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.config;

public class MultiGet implements Get {
	private Get[] getters;

	public MultiGet(Get ... getters) {
		this.getters = getters;
	}

	@Override
	public String get(String name, String def, boolean print) {
		String str;
		for(Get getter : getters) {
			str = getter.get(name, null, print);
			if(str!=null) {
				return str;
			}
		}
		return def;
	}

}
