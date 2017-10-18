/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 * @author jg1555
 *
 */
public class AUTHZServlet<S extends Servlet> implements Servlet {
	private String[] roles;
	private Servlet delegate;

	protected AUTHZServlet(Class<S> cls) {
		try {
			delegate = cls.newInstance();
		} catch (Exception e) {
			delegate = null;
		}
		RolesAllowed rolesAllowed = cls.getAnnotation(RolesAllowed.class);
		if(rolesAllowed == null) {
			roles = null;
		} else {
			roles = rolesAllowed.value();
		}
	}
	
	public void init(ServletConfig sc) throws ServletException {
		if(delegate == null) throw new ServletException("Invalid Servlet Delegate");
		delegate.init(sc);
	}
	
	public ServletConfig getServletConfig() {
		return delegate.getServletConfig();
	}

	public String getServletInfo() {
		return delegate.getServletInfo();
	}

	public void service(ServletRequest req, ServletResponse resp) throws ServletException, IOException {
		if(roles==null) {
			delegate.service(req,resp);
		} else { // Validate
			try {
				HttpServletRequest hreq = (HttpServletRequest)req;
				boolean proceed = false;
				for(String role : roles) {
					if(hreq.isUserInRole(role)) {
						proceed = true;
						break;
					}
				}
				if(proceed) {
					delegate.service(req,resp);
				} else {
					//baseRequest.getServletContext().log(hreq.getUserPrincipal().getName()+" Refused " + roles);
					((HttpServletResponse)resp).sendError(403); // forbidden
				}
			} catch(ClassCastException e) {
				throw new ServletException("JASPIServlet only supports HTTPServletRequest/HttpServletResponse");
			}
		}
	}

	public void destroy() {
		delegate.destroy();
	}


}
