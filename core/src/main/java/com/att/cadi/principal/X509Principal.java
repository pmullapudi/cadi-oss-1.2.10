/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.principal;

import java.io.IOException;
import java.security.Principal;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.regex.Pattern;

import com.att.cadi.GetCred;

public class X509Principal implements Principal, GetCred {
	private static final Pattern pattern = Pattern.compile("[a-zA-Z0-9]*\\@[a-zA-Z0-9.]*");
	private byte[] content;  
	private X509Certificate cert;
	private String name;

	public X509Principal(String identity, X509Certificate cert, byte[] content) {
		name = identity;
		this.content = content;
		this.cert = cert;
	}
	
	public X509Principal(X509Certificate cert, byte[] content) throws IOException {
		this.content=content;
		this.cert = cert;
		String subj = cert.getSubjectDN().getName();
		int cn = subj.indexOf("OU=");
		if(cn>=0) {
			cn+=3;
			int space = subj.indexOf(',',cn);
			if(space>=0) {
				String id = subj.substring(cn, space);
				if(pattern.matcher(id).matches()) {
					name = id;
				}
			}
		}
		if(name==null)
			throw new IOException("X509 does not have Identity as CN");
		
	}
	
	
	public String getAsHeader() throws IOException {
		try {
			if(content==null) 
				content=cert.getEncoded();
		} catch (CertificateEncodingException e) {
			throw new IOException(e);
		}
		return "X509 " + content;
	}
	
	public String toString() {
		return "Cert Authentication for " + cert.toString();
	}


	public byte[] getCred() {
		try {
			return content==null?(content=cert.getEncoded()):content;
		} catch (CertificateEncodingException e) {
			return null;
		}
	}


	public String getName() {
		return name;
	}
}
