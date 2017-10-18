/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.client;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import com.att.inno.env.APIException;
import com.att.inno.env.Data;
import com.att.rosetta.env.RosettaDF;


public interface EClient<CT> {
	public void setMethod(String meth);
	public void setPathInfo(String pathinfo);
	public void setPayload(Transfer transfer);
	public void addHeader(String tag, String value);
	public void setQueryParams(String q);
	public void setFragment(String f);
	public void send() throws APIException;
	public<T> Future<T> futureCreate(Class<T> t);
	public Future<String> futureReadString();
	public<T> Future<T> futureRead(RosettaDF<T> df,Data.TYPE type);
	public<T> Future<T> future(T t);
	public Future<Void> future(HttpServletResponse resp, int expected) throws APIException;
	
	public interface Transfer {
		public void transfer(OutputStream os) throws IOException, APIException;
	}
}
