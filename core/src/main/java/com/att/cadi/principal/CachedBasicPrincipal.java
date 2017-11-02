/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.principal;

import java.io.IOException;

import com.att.cadi.BasicCred;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.taf.HttpTaf;

/**
 * Cached Principals need to be able to revalidate in the Background
 * 
 *
 */
public class CachedBasicPrincipal extends BasicPrincipal implements CachedPrincipal {
	private final HttpTaf creator;
	private long timeToLive;
	private long expires;

	public CachedBasicPrincipal(HttpTaf creator, BasicCred bc, String domain, long timeToLive) {
		super(bc, domain);
		this.creator = creator;
		this.timeToLive = timeToLive;
		expires = System.currentTimeMillis()+timeToLive;
	}
	
	public CachedBasicPrincipal(HttpTaf creator, String content, String domain, long timeToLive) throws IOException {
		super(content, domain);
		this.creator = creator;
		this.timeToLive = timeToLive;
		expires = System.currentTimeMillis()+timeToLive;
	}

	public CachedPrincipal.Resp revalidate() {
		Resp resp = creator.revalidate(this);
		if(resp.equals(Resp.REVALIDATED))expires = System.currentTimeMillis()+timeToLive;
		return resp;
	}

	public long expires() {
		return expires;
	}

}
