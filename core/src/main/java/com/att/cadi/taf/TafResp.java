/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf;

import java.io.IOException;
import java.security.Principal;

import com.att.cadi.Access;
import com.att.cadi.CadiException;

/**
 * Response from Taf objects, which inform users what has happened and/or what should be done
 * 
 * @author jg1555
 *
 */
public interface TafResp {
	public static enum RESP {
		IS_AUTHENTICATED, 
		NO_FURTHER_PROCESSING, 
		TRY_AUTHENTICATING, 
		TRY_ANOTHER_TAF,
		FAIL, 
		// A note was made to avoid the response REDIRECT.  However, I have deemed that it is 
		// unavoidable when the underlying TAF did do a REDIRECT, because it requires a HTTP
		// Service code to exit without modifying the Response any further.
		// Therefore, I have changed this to indicate what HAS happened, with should accommodate 
		// both positions.  JG 10/18/2012
//		public static final int HTTP_REDIRECT_INVOKED = 11;
		HTTP_REDIRECT_INVOKED};
	
	/**
	 * Basic success check
	 * @return
	 */
	public boolean isValid();
	
	/**
	 *  String description of what has occurred (for logging/exceptions)
	 * @return
	 */
	public String desc();
	
	/**
	 * Check Response
	 * @return
	 */
	public RESP isAuthenticated();

	/**
	 * Authenticate, returning FAIL or Other Valid indication
	 * 
	 * HTTP implementations should watch for "HTTP_REDIRECT_INVOKED", and end the HTTP call appropriately.
	 * @return
	 * @throws CadiException 
	 */
	public RESP authenticate() throws IOException;

	/**
	 * Once authenticated, this object should hold a Principal created from the authorization
	 * @return
	 */
	public Principal getPrincipal();

	/**
	 * get the Access object which created this object, allowing the responder to appropriate Log, etc
	 */
	public Access getAccess();
	
	/**
	 * Be able to check if part of a Failed attempt
	 */
	public boolean isFailedAttempt();
}
