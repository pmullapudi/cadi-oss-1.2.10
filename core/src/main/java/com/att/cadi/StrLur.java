/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.util.List;



/**
 * StrLUR: Implements fish with String, skipping the need to be a Principal where it doesn't make sense.
 *
 *
 */
public interface StrLur extends Lur {
	/** 
	 * Fish for Principals in a Pond
	 * 
	 *   or more boringly, is the User identified within a named collection representing permission.
	 * 
	 * @param principalName
	 * @return
	 */
	public boolean fish(String bait, Permission pond);

	/** 
	 * Fish all the Principals out a Pond
	 * 
	 *   For additional humor, pronounce the following with a Southern Drawl, "FishOil"
	 * 
	 *   or more boringly, load the List with Permissions found for Principal
	 * 
	 * @param principalName
	 * @return
	 */
	public void fishAll(String bait, List<Permission> permissions);
}
