/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.client;

import java.net.HttpURLConnection;

import com.att.cadi.http.Retryable;
import com.att.rosetta.env.RosettaDF;

public class Put<T> extends AAFClient.Call<T> {
	public Put(AAFClient ac, RosettaDF<T> df) {
		super(ac,df);
	}
	
	public Result<T> update(final String pathInfo, final T t) throws Exception {
		return client.hman.best(client.ss, 
			 new Retryable<HttpURLConnection, Result<T>>() {
				@Override
				public Result<T> code(Rcli<HttpURLConnection> client) throws Exception {
					Future<T> ft = client.update(pathInfo,df,t); 
					if(ft.get(client.readTimeout)) {
						return Result.ok(ft.code(),ft.value);
					} else {
						return Result.err(ft.code(),ft.body());
					}
				}
			});
	}
	
	public Result<Void> update(final String pathInfo) throws Exception {
		return client.hman.best(client.ss, 
			 new Retryable<HttpURLConnection, Result<Void>>() {
				@Override
				public Result<Void> code(Rcli<HttpURLConnection> client) throws Exception {
					Future<Void> ft = client.update(pathInfo); 
					if(ft.get(client.readTimeout)) {
						return Result.ok(ft.code(),ft.value);
					} else {
						return Result.err(ft.code(),ft.body());
					}
				}
			});
	}

}