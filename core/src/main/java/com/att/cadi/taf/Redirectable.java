/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf;

public interface Redirectable extends TafResp {
	/**
	 * Create a Redirectable URL entry prefaced by a URLEncoder.String for a Menu
	 * example:
	 * "Global Login=https://xxxx....."
	 */
	public String get();
}
