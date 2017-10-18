/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.aaf.v2_0;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Exception;
import com.att.cadi.AbsUserCache;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.GetCred;
import com.att.cadi.Hash;
import com.att.cadi.SecuritySetter;
import com.att.cadi.User;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.config.Config;
import com.att.cadi.dme2.DRcli;
import com.att.cadi.lur.ConfigPrincipal;
import com.att.cadi.lur.aaf.AAFPermission;
import com.att.inno.env.APIException;

public class AAFAuthn extends AbsUserCache<AAFPermission> {
	public static final SecuritySetter<DME2Client> NULL_SECURITY_SETTER = new SecuritySetter<DME2Client>() {
		public void setSecurity(DME2Client client) {
		}
	};
	private DRcli client;
	private int timeout;
	private String realm;
	private long timeToLive;
	
	/**
	 * Configure with Standard AAF properties, Standalone
	 * @param con
	 * @throws Exception 
	 */
	public AAFAuthn(AAFCon con) throws Exception {
		super(con.access,con.cleanInterval,con.highCount,con.usageRefreshTriggerCount);
		client = con.client;
		timeout = con.timeout;
		this.timeToLive = con.cleanInterval;

		try {
			setRealm();
		} catch (APIException e) {
			if(e.getCause() instanceof DME2Exception) {
				// Can't contact AAF, assume default
				realm=con.access.getProperty(Config.AAF_DEFAULT_REALM, Config.getDefaultRealm());
			}
		}
	

	}

	/**
	 * Configure with Standard AAF properties, but share the Cache (with AAF Lur)
	 * @param con
	 * @throws Exception 
	 */
	public AAFAuthn(AAFCon con, AbsUserCache<AAFPermission> cache) throws Exception {
		super(cache);
		client = con.client;
		timeout = con.timeout;
		this.timeToLive = con.cleanInterval;
		try {
			setRealm();
		} catch (Exception e) {
			if(e.getCause() instanceof DME2Exception) {
				// Can't contact AAF, assume default		
				realm=con.access.getProperty(Config.AAF_DEFAULT_REALM, Config.getDefaultRealm());
			}
		}
	}
	
	/*
	 * Configure by parameters
	 * 
	 * @param access
	 * @param url
	 * @param timeout
	 * @param cleanInterval
	 * @param highCount
	 * @throws Exception
	 */
/*	public AAFAuthn(Access access, String url, int timeout,int cleanInterval, int highCount) throws Exception {
		super(access,cleanInterval,highCount);
		client = new DRcli(new URI(url));
		this.timeout = timeout;
		this.timeToLive = cleanInterval;
		try {
			setRealm();
		} catch (APIException e) {
			if(e.getCause() instanceof DME2Exception) {
				// Can't contact AAF, assume default
				realm=access.getProperty(Config.AAF_DEFAULT_REALM, Config.getDefaultRealm());
			}
		}
	}
*/
	private void setRealm() throws Exception {
		// Make a call without security set to get the 401 response, which
		// includes the Realm of the server
		// This also checks on Connectivity early on.
		Future<String> fp = client.forUser(NULL_SECURITY_SETTER).read("/authn/basicAuth", "text/plain");
		if(fp.get(timeout)) {
			throw new Exception("Do not preset Basic Auth Information for AAFAuthn");
		} else {
			if(fp.code()==401) {
				realm = fp.header("WWW-Authenticate");
				if(realm!=null && realm.startsWith("Basic realm=\"")) {
					realm = realm.substring(13, realm.length()-1);
				} else {
					realm = "unknown.com";
				}
			}
		}
	}
	
	/**
	 * Return Native Realm of AAF Instance.
	 * 
	 * @return
	 */
	public String getRealm() {
		return realm;
	}

	/**
	 * Returns null if ok, or an Error String;
	 * 
	 * @param user
	 * @param password
	 * @return
	 * @throws Exception
	 */
	public String validate(String user, String password) throws Exception {
		User<AAFPermission> usr = getUser(user);
		byte[] bytes = password.getBytes();
		if(usr != null && usr.principal != null && usr.principal.getName().equals(user) 
				&& usr.principal instanceof GetCred) {
			
			if(Hash.isEqual(((GetCred)usr.principal).getCred(),bytes)) {
				return null;
			} else {
				remove(usr);
			}
		}
		
		AAFCachedPrincipal cp = new AAFCachedPrincipal(this, user, bytes, timeToLive);
		// Since I've relocated the Validation piece in the Principal, just revalidate, then do Switch
		// Statement
		switch(cp.revalidate()) {
			case REVALIDATED:
				if(usr!=null)usr.principal = cp;
				else addUser(new User<AAFPermission>(cp,timeout));
				return null;
			case INACCESSIBLE:
				return "AAF Inaccessible";
			case UNVALIDATED:
				return "User/Pass combo invalid";
			default: 
				return "AAFAuthn doesn't handle this Principal";
		}
	}
	
	private class AAFCachedPrincipal extends ConfigPrincipal implements CachedPrincipal {
		private AAFAuthn aaf;
		private long expires,timeToLive;

		public AAFCachedPrincipal(AAFAuthn aaf, String name, byte[] pass, long timeToLive) {
			super(name,pass);
			this.aaf = aaf;
			this.timeToLive = timeToLive;
			expires = timeToLive + System.currentTimeMillis();
		}

		public Resp revalidate() {
			try {
				Miss missed = missed(getName());
				if(missed==null || missed.mayContinue(getCred())) {
					Rcli<DME2Client> client = aaf.client.forUser(new DMEPrincipalSS(this));
					Future<String> fp = client.read(
							"/authn/basicAuth",
							"text/plain"
							);
					if(fp.get(aaf.timeout)) {
						expires = System.currentTimeMillis() + timeToLive;
						addUser(new User<AAFPermission>(this, expires));
						return Resp.REVALIDATED;
					} else {
						addMiss(getName(), getCred());
						return Resp.UNVALIDATED;
					}
				} else {
					return Resp.UNVALIDATED;
				}
			} catch (Exception e) {
				aaf.access.log(e);
				return Resp.INACCESSIBLE;
			}
		}

		public long expires() {
			return expires;
		}
	};

}
