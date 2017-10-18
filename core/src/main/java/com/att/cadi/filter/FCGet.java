/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.config.Get;

/*
 * A private method to query the Filter config and if not exists, return the default.  This
 * cleans up the initialization code.
 */
class FCGet implements Get {
	/**
	 * 
	 */
	private final Access access;
	private FilterConfig filterConfig;
	private ServletContext context;

	public FCGet(Access cadiFilter, ServletContext context, FilterConfig filterConfig) {
		this.access = cadiFilter;
		this.context = context;
		this.filterConfig = filterConfig;
	}

	public String get(String name, String def, boolean print) {
		String str = null;
		// Try Server Context First
		if(context!=null) {
			str = context.getInitParameter(name);
		}
		
		// Try Filter Context next
		if(str==null && filterConfig != null) {
			str = filterConfig.getInitParameter(name);
		}
		
		// Take def if nothing else
		if(str==null) {
			str = def;
			// don't log defaults
		} else {
			str = str.trim(); // this is vital in Property File based values, as spaces can hide easily
			if(print)this.access.log(Level.INFO,"Setting", name, "to", str);
		}
		return str;
	}
}
