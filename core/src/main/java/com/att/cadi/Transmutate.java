/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.security.Principal;

/**
 * The unique element of TAF is that we establish the relationship/mechanism to mutate the Principal derived from
 * one Authentication mechanism into a trustable Principal of another.  The mechanism needs to be decided by system
 * trusting.  
 * 
 * The Generic "T" is used so that the code used will be very specific for the implementation, enforced by Compiler
 * 
 * This interface will allow differences of trusting Transmutation of Authentication 
 * @author jg1555
 *
 */
public interface Transmutate<T> {
	/**
	 * Mutate the (assumed validated) Principal into the expected Principal name to be used to construct
	 * 
	 * @param p
	 * @return
	 */
	public T mutate(Principal p);
}
