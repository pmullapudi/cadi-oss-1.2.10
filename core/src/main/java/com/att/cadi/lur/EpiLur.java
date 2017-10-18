/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur;

import java.security.Principal;
import java.util.List;

import com.att.cadi.CachingLur;
import com.att.cadi.CadiException;
import com.att.cadi.CredVal;
import com.att.cadi.Lur;
import com.att.cadi.Permission;

/**
 * EpiLUR
 * 
 * Short for "Epic LUR". Be able to run through a series of LURs to obtain the validation needed.
 * 
 * The pun is better for the other pattern... "TAF" (aka EpiTaf), but it's still the larger picture of 
 * LURs that will be accomplished.
 * 
 * FYI, the reason we separate LURs, rather than combine, is that Various User Repository Resources have
 * different Caching requirements.  For instance, the Local User Repo (with stand alone names), never expire, but might be
 * refreshed with a change in Configuration File, while the Remote Service based LURs will need to expire at prescribed intervals 
 * 
 * @author jg1555
 *
 */
public final class EpiLur implements Lur {
	private final Lur[] lurs;
	
	/**
	 * EpiLur constructor
	 * 
	 * Construct the EpiLur from variable TAF parameters
	 * @param tafs
	 * @throws CadiException
	 */
	public EpiLur(Lur ... tafs) throws CadiException{
		this.lurs = tafs;
		if(tafs.length==0) throw new CadiException("Need at least one Lur implementation in constructor");
	}

	public boolean fish(Principal bait, Permission pond) {
		boolean rv = false;
		Lur lur;
		for(int i=0;!rv && i<lurs.length;++i) {
			rv = (lur = lurs[i]).fish(bait, pond);
			if(!rv && lur.handlesExclusively(pond)) break;
		}
		return rv;
	}

	public void fishAll(Principal bait, List<Permission> permissions) {
		for(Lur lur : lurs) {
			lur.fishAll(bait, permissions);
		}
	}

	public void destroy() {
		for(Lur lur : lurs) {
			lur.destroy();
		}
	}

	/**
	 * Return the first Lur (if any) which also implements UserPass 
	 * @return
	 */
	public CredVal getUserPassImpl() {
		for(Lur lur : lurs) {
			if(lur instanceof CredVal) {
				return (CredVal)lur;
			}
		}
		return null;
	}

	// Never needed... Only EpiLur uses...
	public boolean handlesExclusively(Permission pond) {
		return false;
	}
	
	/**
	 * Get Lur for index.  Returns null if out of range
	 * @param idx
	 * @return
	 */
	public Lur get(int idx) {
		if(idx>=0 && idx<lurs.length) {
			return lurs[idx];
		}
		return null;
	}

	public boolean supports(String userName) {
		for(Lur l : lurs) {
			if(l.supports(userName))return true;
		}
		return false;
	}

	public void remove(String id) {
		for(Lur l : lurs) {
			if(l instanceof CachingLur) {
				((CachingLur<?>)l).remove(id);
			}
		}
	}
}
