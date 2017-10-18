/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

/**
 * UserPass
 * 
 * The essential interface required by BasicAuth to determine if a given User/Password combination is 
 * valid.  This is done as an interface.
 * 
 * @author jg1555
 */
//TODO add a generic "TRANS" so we can do Remote call timings 
public interface CredVal {
	public enum Type{PASSWORD};
	/**
	 *  Validate if the User/Password combination matches records 
	 * @param user
	 * @param pass
	 * @return
	 */
	public boolean validate(String user, Type type, byte[] cred);
}
