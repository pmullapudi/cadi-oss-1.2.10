/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.aaf.v2_0;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.aft.dme2.api.DME2Client;
import com.att.cadi.AbsUserCache;
import com.att.cadi.Access.Level;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.GetCred;
import com.att.cadi.Hash;
import com.att.cadi.Taf.LifeForm;
import com.att.cadi.User;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.cadi.lur.aaf.AAFPermission;
import com.att.cadi.principal.BasicPrincipal;
import com.att.cadi.principal.CachedBasicPrincipal;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.TafResp.RESP;
import com.att.cadi.taf.basic.BasicHttpTafResp;

public class AAFTaf extends AbsUserCache<AAFPermission> implements HttpTaf {
//	private static final String INVALID_AUTH_TOKEN = "Invalid Auth Token";
//	private static final String AUTHENTICATING_SERVICE_UNAVAILABLE = "Authenticating Service unavailable";
	private AAFCon aaf;
	private boolean warn;

	public AAFTaf(AAFCon con, boolean turnOnWarning) {
		super(con.access,con.cleanInterval,con.highCount, con.usageRefreshTriggerCount);
		aaf = con;
		warn = turnOnWarning;
	}

	public AAFTaf(AAFCon con, boolean turnOnWarning, AbsUserCache<AAFPermission> other) {
		super(other);
		aaf = con;
		warn = turnOnWarning;
	}

	public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
		//TODO Do we allow just anybody to validate?

		// Note: Either Carbon or Silicon based LifeForms ok
		String auth = req.getHeader("Authorization");
		if(auth == null) {
			return new BasicHttpTafResp(aaf.access,null,"Requesting HTTP Basic Authorization",RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),false);
		} else  {
			if(warn&&!req.isSecure())aaf.access.log(Level.WARN,"WARNING! BasicAuth has been used over an insecure channel");
			
			try {
				CachedBasicPrincipal bp = new CachedBasicPrincipal(this,auth,aaf.getRealm(),aaf.cleanInterval);
				// First try Cache
				User<AAFPermission> usr = getUser(bp);
				if(usr != null && usr.principal != null) {
					if(usr.principal instanceof GetCred) {
						if(Hash.isEqual(bp.getCred(),((GetCred)usr.principal).getCred())) {
							return new BasicHttpTafResp(aaf.access,bp,bp.getName()+" authenticated by cached AAF password",RESP.IS_AUTHENTICATED,resp,aaf.getRealm(),false);
						}
					}
				}
				
				Miss miss = missed(bp.getName());
				if(miss!=null && !miss.mayContinue(bp.getCred())) {
					return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
							"User/Pass Retry limit exceeded"), 
							RESP.FAIL,resp,aaf.getRealm(),true);
				}
				
				Rcli<DME2Client> userAAF = aaf.client.forUser(new DMEPrincipalSS(bp));
				Future<String> fp = userAAF.read("/authn/basicAuth", "text/plain");
				if(fp.get(aaf.timeout)) {
					if(usr!=null)usr.principal = bp;
					else addUser(new User<AAFPermission>(bp,aaf.cleanInterval));
					return new BasicHttpTafResp(aaf.access,bp,bp.getName()+" authenticated by AAF password",RESP.IS_AUTHENTICATED,resp,aaf.getRealm(),false);
				} else {
					// Note: AddMiss checks for miss==null, and is part of logic
					boolean rv= addMiss(bp.getName(),bp.getCred());
					if(rv) {
						return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
								"User/Pass combo invalid via AAF"), 
								RESP.TRY_AUTHENTICATING,resp,aaf.getRealm(),true);
					} else {
						return new BasicHttpTafResp(aaf.access,null,buildMsg(bp,req,
								"User/Pass combo invalid via AAF - Retry limit exceeded"), 
								RESP.FAIL,resp,aaf.getRealm(),true);
					}
				}
			} catch (IOException e) {
				String msg = buildMsg(null,req,"Invalid Auth Token");
				aaf.access.log(Level.INFO,msg,'(', e.getMessage(), ')');
				return new BasicHttpTafResp(aaf.access,null,msg, RESP.TRY_AUTHENTICATING, resp, aaf.getRealm(),true);
			} catch (Exception e) {
				String msg = buildMsg(null,req,"Authenticating Service unavailable");
				aaf.access.log(Level.INFO,msg,'(', e.getMessage(), ')');
				return new BasicHttpTafResp(aaf.access,null,msg, RESP.FAIL, resp, aaf.getRealm(),false);
			}
		}
	}
	
	private String buildMsg(Principal pr, HttpServletRequest req, Object ... msg) {
		StringBuilder sb = new StringBuilder();
		for(Object s : msg) {
			sb.append(s.toString());
		}
		if(pr!=null) {
			sb.append(" for ");
			sb.append(pr.getName());
		}
		sb.append(" from ");
		sb.append(req.getRemoteAddr());
		sb.append(':');
		sb.append(req.getRemotePort());
		return sb.toString();
	}


	
	public Resp revalidate(CachedPrincipal prin) {
		if(prin instanceof BasicPrincipal) {
			Rcli<DME2Client> userAAF = aaf.client.forUser(new DMEPrincipalSS(prin));
			Future<String> fp;
			try {
				fp = userAAF.read("/authn/basicAuth", "text/plain");
				return fp.get(aaf.timeout)?Resp.REVALIDATED:Resp.UNVALIDATED;
			} catch (Exception e) {
				aaf.access.log(e, "Cannot Revalidate",prin.getName());
				return Resp.INACCESSIBLE;
			}
		}
		return Resp.NOT_MINE;
	}

}
