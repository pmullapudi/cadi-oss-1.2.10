/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.http;

import com.att.cadi.Locator;
import com.att.cadi.client.Rcli;

/**
 * 
 *
 * @param <RT>
 * @param <RET>
 */
public abstract class Retryable<RT,RET> {
	// be able to hold state for consistent Connections.  Not required for all connection types.
	public Rcli<RT> lastClient;
	private Locator.Item item;
	
	public Retryable() {
		lastClient = null;
		item = null;
	}

	public Retryable(Retryable<RT,?> ret) {
		lastClient = ret.lastClient;
		item = ret.item;
	}

	public Locator.Item item(Locator.Item item) {
		lastClient = null;
		this.item = item;
		return item;
	}
	public Locator.Item item() {
		return item;
	}
	
	public abstract RET code(Rcli<RT> client) throws Exception;
}
