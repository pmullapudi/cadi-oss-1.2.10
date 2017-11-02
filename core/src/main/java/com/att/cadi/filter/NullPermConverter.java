/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;


/**
 * A NullPermConverter 
 * 
 * Obey the PermConverter Interface, but passed in "minimal" String is not converted.
 * 
 *
 */
public class NullPermConverter implements PermConverter {

	private NullPermConverter() {}
	private static final NullPermConverter singleton = new NullPermConverter();
	public static NullPermConverter singleton() {return singleton;}

	public String convert(String minimal) {
		return minimal;
	}

}
