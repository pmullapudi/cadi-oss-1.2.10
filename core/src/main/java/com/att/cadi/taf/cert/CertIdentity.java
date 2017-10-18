/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.cert;

import java.security.Principal;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;

public interface CertIdentity {
	/**
	 * identity from X509Certificate Object and/or certBytes
	 * 
	 * If you have both, include them.  If you only have one, leave the other null, and it will be generated if needed
	 * 
	 * The Request is there to obtain Header or Attribute info of ultimate user
	 * 
	 * @param req
	 * @param cert
	 * @param certBytes
	 * @return
	 * @throws CertificateException 
	 */
	public Principal identity(HttpServletRequest req, X509Certificate cert, byte[] certBytes) throws CertificateException;
}
