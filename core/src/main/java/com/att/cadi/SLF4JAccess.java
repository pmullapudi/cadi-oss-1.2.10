/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.att.cadi.config.Config;
import com.att.cadi.config.Get;

public class SLF4JAccess implements Access {
	private static final String AAF_CASS_PLUGIN = "AAF Cass Plugin";
	private static final Logger slf4j = LoggerFactory.getLogger("AAF");
	private final Properties props;
	private Symm decryptor;
	private ClassLoader classLoader;
	
	public SLF4JAccess(final Properties initial) throws CadiException {
		props = initial;
		classLoader = getClass().getClassLoader();
		Config.configPropFiles(new Get() {
			public String get(final String name, final String def, boolean print) {
				String p = props.getProperty(name);
				if(print) {
					if(p==null) log(Level.INIT,AAF_CASS_PLUGIN, name,"is not configured");
					else log(Level.INIT,AAF_CASS_PLUGIN,name,"is set to",p);
				}
				return p;
			}
		}, this);
		// have to setup properties first.
		decryptor = Symm.obtain(this);
	}

	public void log(Level level, Object... elements) {
		switch(level) {
			case AUDIT:
				slf4j.info(msg(elements).toString());
				break;
			case DEBUG:
				slf4j.debug(msg(elements).toString());
				break;
			case ERROR:
				slf4j.error(msg(elements).toString());
				break;
			case INFO:
				slf4j.info(msg(elements).toString());
				break;
			case INIT:
				slf4j.info(msg(elements).toString());
				break;
			case WARN:
				slf4j.warn(msg(elements).toString());
				break;
			default:
				slf4j.info(msg(elements).toString());
				break;
		}
	}
	
	private StringBuilder msg(Object ... elements) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for(Object o : elements) {
			if(first) first = false;
			else {
				sb.append(' ');
			}
			sb.append(o.toString());
		}
		return sb;
	}

	public void log(Exception e, Object... elements) {
		slf4j.error(msg(elements).toString(),e);
	}

	public void setLogLevel(Level level) {
//		switch(level) {
//			case AUDIT:
//				slf4j.setLevel(org.apache.log4j.Level.INFO);
//				break;
//			case DEBUG:
//				slf4j.setLevel(org.apache.log4j.Level.DEBUG);
//				break;
//			case ERROR:
//				slf4j.setLevel(org.apache.log4j.Level.ERROR);
//				break;
//			case INFO:
//				slf4j.setLevel(org.apache.log4j.Level.INFO);
//				break;
//			case INIT:
//				slf4j.setLevel(org.apache.log4j.Level.INFO);
//				break;
//			case WARN:
//				slf4j.setLevel(org.apache.log4j.Level.WARN);
//				break;
//			default:
//				slf4j.setLevel(org.apache.log4j.Level.INFO);
//				break;
//		}
	}

	public ClassLoader classLoader() {
		return classLoader;
	}

	public String getProperty(String string, String def) {
		return props.getProperty(string, def);
	}

	public void load(InputStream is) throws IOException {
		props.clear();
		props.load(is);
	}

	public String decrypt(String encrypted, boolean anytext) throws IOException {
		if(anytext || encrypted.startsWith("enc:")) {
			if(decryptor!=null)
				return decryptor.depass(encrypted);
		}
		return encrypted;
	} 
	
}
