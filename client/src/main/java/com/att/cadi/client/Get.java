/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.client;

import java.net.HttpURLConnection;

import com.att.cadi.http.Retryable;
import com.att.rosetta.env.RosettaDF;

public class Get<T> extends AAFClient.Call<T> {
	public Get(AAFClient ac, RosettaDF<T> df) {
		super(ac,df);
	}
	
	public Result<T> read(final String pathInfo) throws Exception {
		return client.hman.best(client.ss, 
			 new Retryable<HttpURLConnection, Result<T>>() {
				@Override
				public Result<T> code(Rcli<HttpURLConnection> client) throws Exception {
					Future<T> ft = client.read(pathInfo,df); 
					if(ft.get(client.readTimeout)) {
						return Result.ok(ft.code(),ft.value);
					} else {
						return Result.err(ft.code(),ft.body());
					}
				}
			});
	}
}