/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.CachedPrincipal;
import com.att.cadi.Taf.LifeForm;

/**
 * A TAF which is in a specific HTTP environment in which the engine implements 
 * javax Servlet.
 * 
 * Using the Http Request and Response interfaces takes the effort out of implementing in almost any kind of
 * HTTP Container or Engine.
 *  
 *
 */
public interface HttpTaf {
	/**
	 * validate
	 * 
	 * Validate the Request, and respond with created TafResp object.
	 * 
	 * @param reading
	 * @param req
	 * @param resp
	 * @return
	 */
	public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp);
	
	/**
	 * Re-Validate Credential
	 * 
	 * @param prin
	 * @return
	 */
	public CachedPrincipal.Resp revalidate(CachedPrincipal prin);
}
