/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.http;

import java.net.HttpURLConnection;
import java.net.URI;

import com.att.aft.dme2.api.DME2Exception;
import com.att.cadi.CadiException;
import com.att.cadi.Locator;
import com.att.cadi.SecuritySetter;
import com.att.cadi.client.EClient;
import com.att.cadi.client.Rcli;
import com.att.inno.env.APIException;
import com.att.inno.env.Data.TYPE;

/**
 * DME2 Rosetta Client
 * 
 * JAXB defined JSON or XML over DME2 middleware
 * 
 *
 * @param <T>
 */
public class HRcli extends Rcli<HttpURLConnection> {
	 private HMangr hman;

	// Can be more efficient if tied to manager, apparently.  Can pass in null.
	
	protected static final SecuritySetter<HttpURLConnection> NULL_SS = new SecuritySetter<HttpURLConnection>() {
		@Override
		public void setSecurity(HttpURLConnection sslsf) throws CadiException {
		}
	};

	public HRcli(HMangr hman, URI uri) {
		this.uri = uri;
		this.hman = hman;
		type = TYPE.JSON;
		apiVersion = hman.apiVersion();
		ss = NULL_SS; 
	}

	public HRcli(HMangr hman, URI uri, SecuritySetter<HttpURLConnection> secSet) {
		this.uri = uri;
		this.hman = hman;
		ss=secSet;
		type = TYPE.JSON;
		apiVersion = hman.apiVersion();
	}
	
	@Override
	protected HRcli clone(URI uri, SecuritySetter<HttpURLConnection> ss) {
		return new HRcli(hman,uri,ss);
	}



	/**
	 * Note from Thaniga on 11/5.  DME2Client is not expected to be reused... need a fresh one
	 * on each transaction, which is expected to cover the Async aspects.
	 * 
	 * @return
	 * @throws APIException 
	 * @throws DME2Exception 
	 */
	protected EClient<HttpURLConnection> client() throws CadiException {
		try {
			return new HClient(ss,uri,connectionTimeout);
		} catch (Exception e) {
			throw new CadiException(e);
		}
	}
	
	/**
	 * Note from Thaniga on 11/5.  DME2Client is not expected to be reused... need a fresh one
	 * on each transaction, which is expected to cover the Async aspects.
	 * 
	 * @return
	 * @throws APIException 
	 * @throws DME2Exception 
	 */
	protected EClient<HttpURLConnection> client(Locator loc) throws APIException {
		try {
			return new HClient(ss,loc.get(loc.best()),connectionTimeout);
		} catch (Exception e) {
			throw new APIException(e);
		}
	}


	public HRcli setManager(HMangr hman) {
		this.hman = hman;
		return this;
	}

	public String toString() {
		return uri.toString();
	}
	
}
