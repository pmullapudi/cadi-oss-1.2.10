/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.http;

import java.net.HttpURLConnection;

import com.att.cadi.client.Future;

public abstract class HRetry<T> extends Retryable<HttpURLConnection,Future<T>> {
	public HRetry() {}
	public HRetry(HRetry<String> retry) {
		super(retry);
	}
	
}
