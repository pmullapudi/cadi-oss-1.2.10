/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.client;

public class Result<T> {
	public final int code;
	public final T value;
	public final String error;

	private Result(int code, T value, String error) {
		this.code = code;
		this.value = value;
		this.error = error;
	}

	public static<T> Result<T> ok(int code,T t) {
		return new Result<T>(code,t,null);
	}
	
	public static<T> Result<T> err(int code,String body) {
		return new Result<T>(code,null,body);
	}
	
	public boolean isOK() {
		return error==null;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Code: ");
		sb.append(code);
		if(error!=null) {
			sb.append(" = ");
			sb.append(error);
		}
		return sb.toString();
	}
}