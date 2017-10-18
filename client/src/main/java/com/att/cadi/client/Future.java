/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.client;

public abstract class Future<T> {
	public T value;
	public abstract boolean get(int timeout) throws Exception;
	
	public abstract int code();
	public abstract String body();
	public abstract String header(String tag);
}