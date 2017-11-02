/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf;

import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;

import com.att.cadi.Access;

/**
 * A Null Pattern for setting responses to "Deny" before configuration is setup.
 *
 */
class NullTafResp implements TafResp {
	private NullTafResp(){}
	
	private static TafResp singleton = new NullTafResp();
	
	public static TafResp singleton() {
		return singleton;
	}
	
	public boolean isValid() {
		return false;
	}
	
	public RESP isAuthenticated() {
		return RESP.NO_FURTHER_PROCESSING;
	}
	
	public String desc() {
		return "All Authentication denied";
	}
	
	public RESP authenticate() throws IOException {
		return RESP.NO_FURTHER_PROCESSING;
	}

	public Principal getPrincipal() {
		return null;
	}

	public Access getAccess() {
		return new Access() {
			public void log(Level level, Object... elements) {
			}

			public void log(Exception e, Object... elements) {
			}

			public ClassLoader classLoader() {
				return this.classLoader();
			}

			public String getProperty(String string, String def) {
				return null;
			}

			public void load(InputStream is) throws IOException {
			}

			public void setLogLevel(Level level) {
			}

			public String decrypt(String encrypted, boolean anytext) throws IOException {
				return encrypted;
			}
		};
	}

	/* (non-Javadoc)
	 * @see com.att.cadi.taf.TafResp#isFailedAttempt()
	 */
	public boolean isFailedAttempt() {
		return true;
	}
}
