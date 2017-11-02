/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.CadiException;
import com.att.cadi.CredVal;
import com.att.cadi.Lur;
import com.att.cadi.Taf;
import com.att.cadi.config.Config;
import com.att.cadi.config.Get;
import com.att.cadi.lur.EpiLur;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.TafResp;

/**
 * A more Standalone Authentication mechanism.  It will appropriately set
 * HTTPServletResponse for Redirect or Forbidden, as needed.
 * 
 *
 */
public class CadiHTTPManip {
	private HttpTaf taf;
	private Lur lur;
	private CredVal up;
	public CadiHTTPManip(Access access, Object ... additionalTafLurs) throws CadiException {
		Get getter = new AccessGetter(access);
		Config.configPropFiles(getter, access);
		lur = Config.configLur(getter, access, additionalTafLurs);
		if(lur instanceof EpiLur) {
			up = ((EpiLur)lur).getUserPassImpl();
		} else if(lur instanceof CredVal) {
			up = (CredVal)lur;
		} else {
			up = null;
		}
		taf = Config.configHttpTaf(access, getter, up, lur, additionalTafLurs);
	}

	public TafResp validate(HttpServletRequest hreq, HttpServletResponse hresp) throws IOException {
		TafResp tresp = taf.validate(Taf.LifeForm.LFN, hreq, hresp);
		switch(tresp.isAuthenticated()) {
			case IS_AUTHENTICATED:
				break;
			case TRY_AUTHENTICATING:
				switch (tresp.authenticate()) {
					case IS_AUTHENTICATED:
						break;
					case HTTP_REDIRECT_INVOKED:
						break;
					case NO_FURTHER_PROCESSING:
					default:
						hresp.sendError(403, tresp.desc()); // Forbidden
				}
				break;
			case NO_FURTHER_PROCESSING:
				hresp.sendError(403, "Access Denied"); // FORBIDDEN
				break;
			default:
				hresp.sendError(403, "Access Denied"); // FORBIDDEN
		}
		return tresp;
	}

	public Lur getLur() {
		return lur;
	}
}
