/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.security.Principal;

/**
 * Cached Principals need to be able to revalidate in the background.
 * 
 * @author jg1555
 *
 */
public interface CachedPrincipal extends Principal {
	public enum Resp {NOT_MINE,UNVALIDATED,REVALIDATED, INACCESSIBLE};
	
	/**
	 * Re-validate with Creator
	 * 
	 * @return
	 */
	public abstract Resp revalidate();
	
	/**
	 * Store when last updated.
	 * @return
	 */
	public abstract long expires();
}
