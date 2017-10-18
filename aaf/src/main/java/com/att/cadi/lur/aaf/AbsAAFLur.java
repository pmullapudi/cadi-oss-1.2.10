/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import java.net.URI;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.handler.DME2RestfulHandler;
import com.att.cadi.AbsUserCache;
import com.att.cadi.Access;
import com.att.cadi.CadiException;
import com.att.cadi.Lur;
import com.att.cadi.Permission;
import com.att.cadi.Transmutate;
import com.att.cadi.User;
import com.att.cadi.config.Config;
import com.att.rosetta.InJson;



public abstract class AbsAAFLur<PERM extends Permission> extends AbsUserCache<PERM> implements Lur {
	private String[] supports;

	/**
	 *  Need to be able to transmutate a Principal into either ATTUID or MechID, which are the only ones accepted at this
	 *  point by AAF.  There is no "domain", aka, no "@att.com" in "ab1234@att.com".  
	 *  
	 *  The only thing that matters here for AAF is that we don't waste calls with IDs that obviously aren't valid.
	 *  Thus, we validate that the ID portion follows the rules before we waste time accessing AAF remotely
	 */
	protected AbsAAFLur(Access access, String dmeServiceName, String mechUser, String mechPasswd, int dmeTimeOut, long userExpiration, int highCount, int usageRefreshTriggerCount) throws CadiException {
		super(access, userExpiration, highCount,2);
		this.access = access;
		this.dmeServiceName = dmeServiceName;
		
		headers = new HashMap<String,String>();
		// AAF uses JSON exclusively
		headers.put("Content-Type", "application/json");
		//TODO this should ultimately be turned into Standard Credential.  BasicAuth?
		headers.put("Requesting-MechID", mechUser);
		// do we need a MechID Change mechanism?
		headers.put("Requesting-MechID-Password", mechPasswd);
		this.dmeTimeout = dmeTimeOut;
		this.userExpiration = userExpiration;

		supports = access.getProperty(Config.AAF_DOMAIN_SUPPORT, Config.AAF_DOMAIN_SUPPORT_DEF).split("\\s*:\\s*");
	}

	protected final String dmeServiceName;
	protected final HashMap<String, String> headers;
	protected final int dmeTimeout;
	protected static final InJson inJSON = new InJson();
	protected final long userExpiration;
	protected final Access access;
	protected static final Transmutate<Principal> transmutate = new AAFTransmutate();

	protected abstract User<PERM> loadUser(Principal bait) throws Exception;
	protected abstract boolean isCorrectPermType(Permission pond);
	
	public /*final*/ boolean supports(String userName) {
		if(userName!=null) {
			for(String s : supports) {
				if(userName.endsWith(s))
					return true;
			}
		}
		return false;
	}

	// This is where you build AAF CLient Code.  Answer the question "Is principal "bait" in the "pond"
	public boolean fish(Principal bait, Permission pond) {
		if(supports(bait.getName())) {
			User<PERM> user = getUser(bait);
			if(user==null)
				try {
					user = loadUser(bait);
				} catch (Exception e) {
					access.log(e, "Failure to load from AAF Service");
				}
			return user==null?false:user.contains(pond);
		} else {
			return false;
		}
	}

	public void fishAll(Principal bait, List<Permission> perms) {
		if(supports(bait.getName())) {
	
			User<PERM> user = getUser(bait);
			if(user==null)
				try {
					user = loadUser(bait);
				} catch (Exception e) {
					access.log(e, "Failure to load from AAF Service");
				}
			if(user!=null) {
				user.copyPermsTo(perms);
			}
		}
	}


	/**
	 * Note: I discovered from Thaniga on 11/5/13 that the DME2Client is not meant to be reused
	 * Need to create a fresh one everytime.
	 * Previous code to reuse Clients has been removed
	 * 
	 * @return
	 * @throws CadiException
	 */
	protected DME2Client newClient() throws CadiException {
		DME2Client sender;
		try {
			sender = new DME2Client(new URI(dmeServiceName), dmeTimeout);
			DME2RestfulHandler replyHandler = new DME2RestfulHandler(dmeServiceName);
			sender.setReplyHandler(replyHandler);
			sender.setAllowAllHttpReturnCodes(true);
		} catch (Exception e) {
			throw new CadiException(e);
		}
		// Set the HttpRequest headers
		sender.setHeaders(headers);
		//Set method
		sender.setMethod("GET");
		return sender;
	}

}
