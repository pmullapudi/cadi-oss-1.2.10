/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.att.cadi.Access.Level;
import com.att.cadi.filter.NullPermConverter;
import com.att.cadi.filter.PermConverter;
import com.att.cadi.lur.EpiLur;
import com.att.cadi.lur.LocalPermission;
import com.att.cadi.taf.TafResp;



/**
 * Inherit the HttpServletRequestWrapper, which calls methods of delegate it's created with, but
 * overload the key security mechanisms with CADI mechanisms
 * 
 * This works with mechanisms working strictly with HttpServletRequest (i.e. Servlet Filters)
 * 
 * Specialty cases, i.e. Tomcat, which for their containers utilize their own mechanisms and Wrappers, you may
 * need something similar.  See AppServer specific code (i.e. tomcat) for these.
 * 
 * @author jg1555
 *
 */
public class CadiWrap extends HttpServletRequestWrapper implements HttpServletRequest, BasicCred {
	private Principal principal;
	private Lur lur;
	private String user; // used to set user/pass from brain-dead protocols like WSSE 
	private byte[] password;
	private PermConverter pconv;
	private Access access; 
	
	/**
	 * Standard Wrapper constructor for Delegate pattern
	 * @param request
	 */
	public CadiWrap(HttpServletRequest request, TafResp tafResp, Lur lur) {
		super(request);
		principal = tafResp.getPrincipal();
		access = tafResp.getAccess();
		this.lur = lur;
		pconv = NullPermConverter.singleton();
	}

	/**
	 * Standard Wrapper constructor for Delegate pattern, with PermConverter
	 * @param request
	 */
	public CadiWrap(HttpServletRequest request, TafResp tafResp, Lur lur, PermConverter pc) {
		super(request);
		principal = tafResp.getPrincipal();
		access = tafResp.getAccess();
		this.lur = lur;
		pconv = pc;
	}


	/**
	 * Part of the HTTP Security API.  Declare the User associated with this HTTP Transaction.
	 * CADI does this by reporting the name associated with the Principal obtained, if any.
	 */
// @Override
	public String getRemoteUser() {
		return principal==null?null:principal.getName();
	}

	/**
	 * Part of the HTTP Security API.  Return the User Principal associated with this HTTP 
	 * Transaction.
	 */
// @Override
	public Principal getUserPrincipal() {
		return principal;
	}
	
	/**
	 * This is the key API call for AUTHZ in J2EE.  Given a Role (String passed in), is the user
	 * associated with this HTTP Transaction allowed to function in this Role?
	 * 
	 * For CADI, we pass the responsibility for determining this to the "LUR", which may be
	 * determined by the Enterprise.
	 * 
	 * Note: Role check is also done in "CadiRealm" in certain cases...
	 * 
	 *
	 */
// @Override
	public boolean isUserInRole(String perm) {
		return checkPerm(access,"Servlet.isUserInRole(permission)",principal,pconv,lur,perm);
	}
	
	public static boolean checkPerm(Access access, String caller, Principal principal, PermConverter pconv, Lur lur, String perm) {
		if(principal== null) {
			access.log(Level.AUDIT,caller, "No Principal in Transaction");
			return false;
		} else { 
			perm = pconv.convert(perm);
			if(lur.fish(principal,new LocalPermission(perm))) {
				access.log(Level.DEBUG,caller, principal.getName(), "in '", perm, '\'');
				return true;
			} else {
				access.log(Level.DEBUG,caller, principal.getName(), "not in '", perm,'\'');
				return false;
			}
		}

	}

	/** 
	 * CADI Function (Non J2EE standard). GetPermissions will read the Permissions from AAF (if configured) and Roles from Local Lur, etc
	 *  as implemented with lur.fishAll
	 *  
	 *  To utilize, the Request must be a "CadiWrap" object, then call.
	 */
	public List<Permission> getPermissions(Principal p) {
		List<Permission> perms = new ArrayList<Permission>();
		lur.fishAll(p, perms);
		return perms;
	}
	/**
	 * Allow setting of tafResp and lur after construction
	 * 
	 * This can happen if the CadiWrap is constructed in a Valve other than CadiValve
	 */
	public void set(TafResp tafResp, Lur lur) {
		principal = tafResp.getPrincipal();
		access = tafResp.getAccess();
		this.lur = lur;
	}

	public String getUser() {
		return user;
	}

	public byte[] getCred() {
		return password;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public void setCred(byte[] passwd) {
		password = passwd;
	}
	
	public CadiWrap setPermConverter(PermConverter pc) {
		pconv = pc;
		return this;
	}
	
	// Add a feature
	public void invalidate(String id) {
		if(lur instanceof EpiLur) {
			((EpiLur)lur).remove(id);
		} else if(lur instanceof CachingLur) {
			((CachingLur<?>)lur).remove(id);
		}
	}
}
