/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;


public abstract class AbsCachedPrincipal<TAF> implements CachedPrincipal {
	protected TAF taf;

	protected AbsCachedPrincipal(TAF taf) {
		this.taf = taf;
	}

}
