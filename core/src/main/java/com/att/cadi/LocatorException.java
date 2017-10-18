/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

public class LocatorException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4267929804321134469L;

	public LocatorException(String arg0) {
		super(arg0);
	}

	public LocatorException(Throwable arg0) {
		super(arg0);
	}

	public LocatorException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public LocatorException(CharSequence cs) {
		super(cs.toString());
	}

}
