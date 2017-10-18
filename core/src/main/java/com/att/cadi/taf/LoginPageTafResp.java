/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.Locator;
import com.att.cadi.Locator.Item;

public class LoginPageTafResp extends AbsTafResp {
	private final HttpServletResponse httpResp;
	private final String loginPageURL;

	private LoginPageTafResp(Access access, final HttpServletResponse resp, String loginPageURL) {
		super(access, null, "Multiple Possible HTTP Logins available.  Redirecting to Login Choice Page");
		httpResp = resp;
		this.loginPageURL = loginPageURL;
	}

	@Override
	public RESP authenticate() throws IOException {
		httpResp.sendRedirect(loginPageURL);
		return RESP.HTTP_REDIRECT_INVOKED;
	}
	
	@Override
	public RESP isAuthenticated() {
		return RESP.TRY_AUTHENTICATING;
	}
	
	public static TafResp create(Access access, Locator locator, final HttpServletResponse resp, List<Redirectable> redir) {
		if(locator!=null) {
			try {
				Item item = locator.best();
				URI uri = locator.get(item);
				if(uri!=null) {
					StringBuilder sb = new StringBuilder(uri.toString());
					String query = uri.getQuery();
					boolean first = query==null || query.length()==0;
					int count=0;
					for(Redirectable t : redir) {
						if(first) {
							sb.append('?');
							first=false;
						}
						else sb.append('&');
						sb.append(t.get());
						++count;
					}
					if(count>0)return new LoginPageTafResp(access, resp, sb.toString());
				}
			} catch (Exception e) {
				access.log(e, "Error deriving Login Page location");
			}
		} else if(!redir.isEmpty()) { 
			access.log(Level.DEBUG,"LoginPage Locator is not configured. Taking first Redirectable Taf");
			return redir.get(0);
		}
		return NullTafResp.singleton();
	}
}
