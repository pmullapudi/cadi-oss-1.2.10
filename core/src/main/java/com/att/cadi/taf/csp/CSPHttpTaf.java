/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.csp;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.CadiException;
import com.att.cadi.config.Config;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.PuntTafResp;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.csp.CSPTafResp.Creator;
import com.att.cadi.taf.localhost.LocalhostTaf;

public class CSPHttpTaf extends CSPTaf implements HttpTaf {
	private boolean acceptLocalhost;

	/**
	 * 
	 * @param hostname
	 * @param prod
	 * @throws CadiException
	 */
	public CSPHttpTaf(Access access, String hostname, String cspEnv, boolean devlLocalhost) throws CadiException {
		super(access, hostname, cspEnv);
		acceptLocalhost = devlLocalhost;
	}

	public TafResp validate(LifeForm reading, HttpServletRequest req, final HttpServletResponse resp) {
		// CSP is currently used only for Human interactions
		String attessec = null;
		String atteshr = null;
		String authHeader = req.getHeader("Authorization");
		String requestURL = req.getRequestURL().toString();
		if(authHeader!=null && authHeader.startsWith("CSP ")) {
			attessec = authHeader.substring(4);
		} else {
			Cookie[] cookies = req.getCookies();
			if(cookies!=null) {
				for(Cookie cookie : cookies) {
					String cookieName = cookie.getName();
					if("attESSec".equalsIgnoreCase(cookieName)) {
						attessec = cookie.getValue();
					} else if("attESHr".equalsIgnoreCase(cookieName)) {
						atteshr = cookie.getValue();
					}
				}
			}
		}
		if(attessec != null) { // atteshr can be null
			return validate(
					new Creator() {
//						@Override
						public TafResp create(Access access, Principal principal, String desc, String remdialURL) {
							return new CSPHttpTafResp(access,principal,desc, resp, remdialURL);
						}},
					attessec,
					requestURL, // Note: since we are taking the URL from the Request, we assume encoded correctly
					atteshr // this can be null
					);
		} else {
			//access.log("Cookie not found");
			// If no cookie, and it's a Silicon Based Lifeform, punt now, because we can't do webpage redirects...
			if(reading == LifeForm.SBLF)return PuntTafResp.singleton();
				// Normally, we don't accept incoming Localhost Web Pages, because CSP loops (infinite?)
				// However, we must allow Developers to utilize creating an entry in 
				// /etc/hosts file:   127.0.0.1   <mymachine>.att.com
				// So we'll allow something that resolves to 127... if they have it there, but they must use that
				// name or CSP will loop
			if(acceptLocalhost) {
				if(requestURL.contains("localhost") || requestURL.contains("127.0.0.1") || requestURL.contains("::1")) 
					return PuntTafResp.singleton();
			} else if(LocalhostTaf.isLocalAddress(req.getRemoteAddr()))	// normal mode... punt any incoming localhost
				return PuntTafResp.singleton();

			// Need full URI for CSP... However, ServletRequest doesn't expose, so we'll have to rebuild it... sigh.
			String remedialURL = requestURL;
			String qs;
			if((qs = req.getQueryString())!=null) {
				try {
					remedialURL = URLEncoder.encode(requestURL+'?'+qs,Config.UTF_8);
				} catch (UnsupportedEncodingException e) {
					// Not a real possibility after initial Development... not worth trying to log
				}
			}
			

			return new CSPHttpTafResp(
					access,
					null, // no principal
					"attESSec cookie does not exist",
					resp,
					cspurl + "/?retURL=" + remedialURL + "&sysName=" + hostname);
		}

	}

	public Resp revalidate(CachedPrincipal prin) {
		// We always return NOT MINE, because we can't redo the Web Page. However, the CSP Principal will
		// validate it's expiration
		return Resp.NOT_MINE;
	}

}
