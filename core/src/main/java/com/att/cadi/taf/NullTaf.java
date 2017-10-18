/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.Taf;


/**
 * This TAF is set at the very beginning of Filters and Valves so that if any configuration issues hit while
 * starting, the default behavior is to shut down traffic rather than leaving an open hole
 * 
 * @author jg1555
 *
 */
public class NullTaf implements Taf, HttpTaf {
	// Singleton Pattern
	private NullTaf() {}
	private static NullTaf singleton = new NullTaf();
	public static NullTaf singleton() {return singleton;}

	/**
	 * validate 
	 * 
	 * Always Respond with a NullTafResp, which declares it is unauthenticated, and unauthorized
	 */
	public TafResp validate(LifeForm reading, String... info) {
		return NullTafResp.singleton();
	}

	/**
	 * validate 
	 * 
	 * Always Respond with a NullTafResp, which declares it is unauthenticated, and unauthorized
	 */
	public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
		return NullTafResp.singleton();
	}

	public Resp revalidate(CachedPrincipal prin) {
		return Resp.NOT_MINE;
	}
}
