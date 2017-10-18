/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.att.cadi.lur.LocalPermission;

/**
 * Class to hold info from the User Perspective.
 * 
 * @author jg1555
 *
 */
public final class User<PERM extends Permission> {
	private static Map<String,Permission> NULL_MAP = new HashMap<String,Permission>();
	public Principal principal;
	Map<String, Permission> perms ;
	long permExpires;
	private final long interval;
	int count;
	
	// Note: This should only be used for Local RBAC (in memory)
	public User(Principal principal) {
		this.principal = principal;
		perms = NULL_MAP;
		permExpires = Long.MAX_VALUE; // Never.  Well, until 64 bits of millis since 1970 expires...
		interval = 0L;
		count = 0;
	}

	public User(Principal principal, long expireInterval) {
		this.principal = principal;
		perms = NULL_MAP;
		expireInterval = Math.max(expireInterval, 0); // avoid < 1
		interval = Math.max(AbsUserCache.MIN_INTERVAL,Math.min(expireInterval,AbsUserCache.MAX_INTERVAL));
		permExpires = 0;
		count = 0;
	}
	
	public void renewPerm() {
		permExpires = System.currentTimeMillis()+interval;
	}
	
	public long permExpires() {
		return permExpires;
	}
	
	public boolean permExpired() {
		return System.currentTimeMillis() > permExpires;
	}

	public boolean noPerms() {
		return perms==null || perms.values().size()==0; 
	}

	public synchronized void incCount() {
		++count;
	}
	
	public synchronized void resetCount() {
		count=0;
	}
	
	public Map<String,Permission> newMap() {
		return new ConcurrentHashMap<String,Permission>();
	}

	public void add(LocalPermission permission) {
		if(perms==NULL_MAP)perms=newMap();
		perms.put(permission.getKey(),permission);
	}

	public void add(Map<String, Permission> newMap, PERM permission) {
		newMap.put(permission.getKey(),permission);
	}

	public void setMap(Map<String, Permission> newMap) {
		perms = newMap;
	}

	public boolean contains(Permission perm) {
		for (Permission p : perms.values()) {
			if (p.match(perm)) return true;
		}
		return false;
	}
	
	public void copyPermsTo(List<Permission> sink) {
		sink.addAll(perms.values());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(principal.getName());
		sb.append('|');
		boolean first = true;
		synchronized(perms) {
			for(Permission gp : perms.values()) {
				if(first) {
					first = false;
					sb.append(':');
				} else {
					sb.append(',');
				}
				sb.append(gp.getKey());
			}
		}
		return sb.toString();
	}

}