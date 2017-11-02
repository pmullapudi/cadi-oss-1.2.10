/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

/**
 * Interface to add a User Chain String to Principal
 * 
 * Format:<APP>:<ID>:<protocol>[:AS][,<APP>:<ID>:<protocol>]
 * 
 *  Where
 *  APP is name suitable for Logging (i.e. official App Acronym) 
 *  ID is official User or MechID, best if includes Identity Source (i.e. ab1234@csp.att.com)
 *  Protocol is the Security protocol,
 * 
 *
 */
public interface UserChain  {
	public enum Protocol {BasicAuth,Cookie,Cert,OAuth};
	public String userChain();
}
