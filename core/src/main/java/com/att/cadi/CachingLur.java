/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import com.att.cadi.CachedPrincipal.Resp;


public interface CachingLur<PERM extends Permission> extends Lur {
	public abstract void remove(String user);
	public abstract Resp reload(User<PERM> user);
	public abstract void setDebug(String commaDelimIDsOrNull);
}
