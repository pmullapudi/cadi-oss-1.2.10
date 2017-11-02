/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.att.cadi.AbsUserCache;
import com.att.cadi.Access;
import com.att.cadi.CredVal;
import com.att.cadi.Hash;
import com.att.cadi.Permission;
import com.att.cadi.StrLur;
import com.att.cadi.User;
import com.att.cadi.config.Config;


/**
 * An in-memory Lur that can be configured locally with User info via properties, similar to Tomcat-users.xml mechanisms.
 * 
 *
 */
public final class LocalLur extends AbsUserCache<LocalPermission> implements CredVal, StrLur {
	public static final String SEMI = "\\s*;\\s*";
	public static final String COLON = "\\s*:\\s*";
	public static final String COMMA = "\\s*,\\s*";
	public static final String PERCENT = "\\s*%\\s*";
	
	// Use to quickly determine whether any given group is supported by this LUR
	private final Set<String> supportingGroups;
	private String supportedRealm; 
	
	/**
	 * Construct by building structure, see "build"
	 * 
	 * Reconstruct with "build"
	 * 
	 * @param userProperty
	 * @param groupProperty
	 * @param decryptor
	 * @throws IOException
	 */
	public LocalLur(Access access, String userProperty, String groupProperty) throws IOException {
		super(access, 0, 0, Integer.MAX_VALUE);  // data doesn't expire
		supportedRealm = access.getProperty(Config.BASIC_REALM, "localized");
		supportingGroups = new TreeSet<String>();
		
		if(userProperty!=null) {
			// For each User name...
			for(String user : userProperty.trim().split(SEMI)) {
				String[] us = user.split(COLON,2);
				String[] userpass = us[0].split(PERCENT,2);
				String u;
				User<LocalPermission> usr;
				if(userpass.length>1) {
					if(userpass.length>0 && userpass[0].indexOf('@')<0) {
						userpass[0]=userpass[0] + '@' + access.getProperty(Config.AAF_DEFAULT_REALM,Config.getDefaultRealm());
					}

					u = userpass[0];
					byte[] pass = access.decrypt(userpass[1], true).getBytes();
					usr = new User<LocalPermission>(new ConfigPrincipal(u, pass));
				} else {
					u = us[0];
					usr = new User<LocalPermission>(new ConfigPrincipal(u, (byte[])null));
				}
				addUser(usr);
				
				if(us.length>1) {
					Map<String, Permission> newMap = usr.newMap();
					for(String group : us[1].split(COMMA)) {
						supportingGroups.add(group);
						usr.add(newMap,new LocalPermission(group));
					}
					usr.setMap(newMap);
				}
			}
		}
		if(groupProperty!=null) {
			// For each Group name...
			for(String group : groupProperty.trim().split(SEMI)) {
				String[] gs = group.split(COLON,2);
				if(gs.length>1) {
					supportingGroups.add(gs[0]);
					LocalPermission p = new LocalPermission(gs[0]);
					// Add all users (known by comma separators)	
					
					for(String grpMem : gs[1].split(COMMA)) {
						// look for password, if so, put in passMap
						String[] userpass = grpMem.split(PERCENT,2);
						if(userpass.length>0 && userpass[0].indexOf('@')<0) {
							userpass[0]=userpass[0] + '@' + access.getProperty(Config.AAF_DEFAULT_REALM,Config.getDefaultRealm());
						}
						User<LocalPermission> usr = getUser(userpass[0]);
						if(userpass.length>1) {
							byte[] pass = access.decrypt(userpass[1], true).getBytes();
							if(usr==null)addUser(usr=new User<LocalPermission>(new ConfigPrincipal(userpass[0],pass)));
							else usr.principal=new ConfigPrincipal(userpass[0],pass);
						} else {
							if(usr==null)addUser(usr=new User<LocalPermission>(new ConfigPrincipal(userpass[0],(byte[])null)));
						}
						usr.add(p);
					}
				}
			}
		}
	}
	
	public boolean validate(String user, Type type, byte[] cred) {
		User<LocalPermission> usr = getUser(user);
		switch(type) {
			case PASSWORD:
				// covers null as well as bad pass
				if(usr!=null && cred!=null && usr.principal instanceof ConfigPrincipal) {
					return Hash.isEqual(cred,((ConfigPrincipal)usr.principal).getCred());
				}
				break;
		}
		return false;
	}

	//	@Override
	public boolean fish(Principal bait, Permission pond) {
		if(supports(bait.getName()) && pond instanceof LocalPermission) { // local Users only have LocalPermissions
				User<LocalPermission> user = getUser(bait);
				return user==null?false:user.contains((LocalPermission)pond);
			}
		return false;
	}

	public boolean fish(String bait, Permission pond) {
		if(supports(bait) && pond instanceof LocalPermission) { // local Users only have LocalPermissions
			User<LocalPermission> user = getUser(bait);
			return user==null?false:user.contains((LocalPermission)pond);
		}
		return false;
	}

	// We do not want to expose the actual Group, so make a copy.
	public void fishAll(Principal bait, List<Permission> perms) {
		if(supports(bait.getName())) {
			User<LocalPermission> user = getUser(bait);
			if(user!=null) {
				user.copyPermsTo(perms);
			}
		}
	}

	public void fishAll(String bait, List<Permission> perms) {
		if(supports(bait)) {
			User<LocalPermission> user = getUser(bait);
			if(user!=null) {
				user.copyPermsTo(perms);
			}
		}
	}

	public boolean supports(String userName) {
		return userName!=null && userName.endsWith(supportedRealm);
	}

	public boolean handlesExclusively(Permission pond) {
		return supportingGroups.contains(pond.getKey());
	}

}
