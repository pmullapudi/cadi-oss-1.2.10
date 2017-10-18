/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.aaf.cass;

import java.security.Principal;

import org.apache.cassandra.auth.AuthenticatedUser;

import com.att.cadi.Access;

public class AAFAuthenticatedUser extends AuthenticatedUser implements Principal {
	private boolean anonymous = false, supr=false, local=false;
	private String fullName;
//	private Access access;

	public AAFAuthenticatedUser(Access access, String name) {
		super(name);
//		this.access = access;
	    int endIndex = name.indexOf("@");
	    if(endIndex >= 0) {
	    	fullName = name;
	    } else {
	    	fullName = name + '@' + AAFBase.default_realm;
	    }
	}
	
	public String getFullName() {
		return fullName;
	}
	
	/* (non-Javadoc)
	 * @see org.apache.cassandra.auth.AuthenticatedUser#isAnonymous()
	 */
	@Override
	public boolean isAnonymous() {
		return anonymous;
	}

	public void setAnonymous(boolean anon) {
		anonymous = anon;
	}

	public boolean getAnonymous() {
		return anonymous;
	}

	/* (non-Javadoc)
	 * @see org.apache.cassandra.auth.AuthenticatedUser#isSuper()
	 */
	@Override
	public boolean isSuper() {
		return supr;
	}

	public void setSuper(boolean supr) {
		this.supr = supr;
	}

	public boolean getSuper() {
		return supr;
	}

	/**
	 * We check Local so we can compare with the right Lur.  This is AAF Plugin only.
	 * @return
	 */
	public boolean isLocal() {
		return local;
	}
	
	public void setLocal(boolean val) {
		local = val;
	}

	@Override
	  public boolean equals(Object o) {
		  if (this == o) return true;
	      if (!(o instanceof AAFAuthenticatedUser)) return false;
	      return ((AuthenticatedUser)o).getName().equals(this.getName());
	  }

	  @Override
	  public int hashCode() {
		  //access.log(Level.DEBUG, "AAFAuthentication hashcode ",getName().hashCode());
	      return getName().hashCode();
	  }  
}
