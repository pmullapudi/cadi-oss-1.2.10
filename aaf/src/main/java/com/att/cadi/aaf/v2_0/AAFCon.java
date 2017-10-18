/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.aaf.v2_0;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import aaf.v2_0.Certs;
import aaf.v2_0.Perms;
import aaf.v2_0.Users;

import com.att.aaf.marshal.CertsMarshal;
import com.att.aft.dme2.api.DME2Client;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.SecuritySetter;
import com.att.cadi.config.Config;
import com.att.cadi.dme2.DRcli;
import com.att.inno.env.APIException;
import com.att.rosetta.env.RosettaDF;
import com.att.rosetta.env.RosettaEnv;

public class AAFCon {
	final public Access access;
	// Package access
	final public int timeout, cleanInterval;
	final public int highCount, userExpires, usageRefreshTriggerCount;
	final public DRcli client;
	final public RosettaDF<Perms> permsDF;
	final public RosettaDF<Certs> certsDF;
	final public RosettaDF<Users> usersDF;
	private String realm;

	public AAFCon(Access access) throws APIException, URISyntaxException{
		this.access = access;
		timeout = Integer.parseInt(access.getProperty(Config.AAF_READ_TIMEOUT, Config.AAF_READ_TIMEOUT_DEF));
		cleanInterval = Integer.parseInt(access.getProperty(Config.AAF_CLEAN_INTERVAL, Config.AAF_CLEAN_INTERVAL_DEF));
		highCount = Integer.parseInt(access.getProperty(Config.AAF_HIGH_COUNT, Config.AAF_HIGH_COUNT_DEF).trim());
		int dmeTimeout = Integer.parseInt(access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF).trim());
		userExpires = Integer.parseInt(access.getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF).trim());
		usageRefreshTriggerCount = Integer.parseInt(access.getProperty(Config.AAF_USER_EXPIRES, Config.AAF_USER_EXPIRES_DEF).trim())-1; // zero based

		
		String aafurl = access.getProperty(Config.AAF_URL,null);
		if(aafurl==null) throw new APIException(Config.AAF_URL + " property is required.");
		// timeout?
		String user = access.getProperty(Config.AAF_MECHID, null);
		String password;
		SecuritySetter<DME2Client> ss = DRcli.NULL_SS;
		if(user==null) {
			access.log(Level.INIT,Config.AAF_MECHID,"is not set");
		} else {
			if((password = access.getProperty(Config.AAF_MECHPASS, null))==null) {
				access.log(Level.INIT,Config.AAF_MECHPASS,"is not set");
			} else {
				try {
					password = access.decrypt(password, true);
					ss = new UserPass(user,password); 
				} catch (IOException e) {
					throw new APIException("Error decrypting " + Config.AAF_MECHPASS,e);
				}
			}
		}
		client = new DRcli(new URI(aafurl),ss);
		client.apiVersion("2.0")
			  .readTimeout(dmeTimeout);
		// Make a call without security set to get the 401 response, which
		// includes the Realm of the server
		// This also checks on Connectivity early on.
//		Future<String> fp = client.read("/authn/basicAuth", "text/plain");
//		if(fp.get(timeout)) {
//			throw new APIException("AAF Auth Service unavailable for use");
//		} else {
//			if(fp.info.code==401) {
//				realm = fp.info.headers.get("WWW-Authenticate");
//				if(realm!=null && realm.startsWith("Basic realm=\"")) {
//					realm = realm.substring(13, realm.length()-1);
//				} else {
//					realm = "unknown.com";
//				}
//			}
//		}
		realm="aaf.att.com";

		RosettaEnv env = new RosettaEnv();
		permsDF = env.newDataFactory(Perms.class);
		usersDF = env.newDataFactory(Users.class);
		certsDF = env.newDataFactory(Certs.class);
		certsDF.rootMarshal(new CertsMarshal()); // Speedier Marshaling
	}

//	public AAFCon(Access access,  String aafurl, int timeout, int cleanInterval, int highCount) throws URISyntaxException, APIException  {
//		this.access = access;
//		this.timeout = timeout;
//		this.cleanInterval = cleanInterval;
//		this.highCount = highCount;
//		int dmeTimeout = Integer.parseInt(access.getProperty(Config.AAF_CONN_TIMEOUT, Config.AAF_CONN_TIMEOUT_DEF));
//		// timeout?
//		(client = new DRcli(new URI(aafurl)))
//			.apiVersion("2.0")
//			.readTimeout(dmeTimeout);
//		realm="aaf.att.com";
//
//		RosettaEnv env = new RosettaEnv();
//		permsDF = env.newDataFactory(Perms.class);
//	}
//
	public String getRealm() {
		return realm;

	}

	public void basicAuth(final String user, final String pass) throws IOException {
		client.setSecuritySetter(new UserPass(user, pass));
	}
	
	public void set(SecuritySetter<DME2Client> ss) {
		client.setSecuritySetter(ss);
	}
}
