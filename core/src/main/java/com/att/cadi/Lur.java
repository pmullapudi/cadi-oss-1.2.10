/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.security.Principal;
import java.util.List;



/**
 * LUR: Local User Registry
 *
 * Concept by Robert Garskof, Implementation by Jonathan Gathman
 * 
 * Where we can keep local copies of users and roles for faster Authorization when asked.
 * 
 * Note: Author cannot resist the mental image of using a Fishing Lure to this LUR pattern 
 * 
 *
 */
public interface Lur {
	/** 
	 * Fish for Principals in a Pond
	 * 
	 *   or more boringly, is the User identified within a named collection representing permission.
	 * 
	 * @param principalName
	 * @return
	 */
	public boolean fish(Principal bait, Permission pond);

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
	public void fishAll(Principal bait, List<Permission> permissions);

	/**
	 * Allow implementations to disconnect, or cleanup resources if unneeded
	 */
	public void destroy();

	/**
	 * Does this LUR handle this pond exclusively?  Important for EpiLUR to determine whether 
	 * to try another (more expensive) LUR 
	 * @param pond
	 * @return
	 */
	public boolean handlesExclusively(Permission pond);  
	
	/**
	 * What domain of User does this LUR support?  (used to avoid asking when not possible)
	 * 
	 * @param bait
	 * @return
	 */
	public boolean supports(String userName);

}
