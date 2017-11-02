/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;


/**
 *  Apply any particular security mechanism
 *  
 *  This allows the definition of various mechanisms involved outside of DRcli jars 
 *  
 *
 */
public interface SecuritySetter<CT> {
	void setSecurity(CT client) throws CadiException;
}
