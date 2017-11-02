/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.wsse;

/**
 * Interface to specify an action deep within a parsing tree on a local object
 * 
 * We use a Generic so as to be flexible on create what that object actually is.  This is passed in at the
 * root "parse" call of Match.  Similar to a "Visitor" Pattern, this object is passed upon reaching the right
 * point in a parse tree.
 * 
 *
 * @param <OUTPUT>
 */
interface Action<OUTPUT> {
	public boolean content(OUTPUT output, String text);
}