/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;

/**
 * Convert a simplistic, single string Permission into an Enterprise Scoped Perm
 * 
 *
 */
public interface PermConverter {
	public String convert(String minimal);
}
