/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.config;

import java.lang.reflect.Method;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;

public interface Get {
	public String get(String name, String def, boolean print);
	
	
	/**
	 * A class for Getting info out of "JavaBean" format
	 *
	 */
	public static class Bean implements Get {
		private Object bean;
		private Class<?> bc;
		private Class<?>[] params;
		private Object[] args;
		private Access ca;
		
		public Bean(Object bean, Access access) {
			this.bean = bean;
			bc = bean.getClass();
			params = new Class<?>[0]; // note, this will allow to go out of scope after config
			args = new Object[0];
			ca = access;
		}
		
		public String get(String name, String def, boolean print) {
			String str = ca.getProperty(name, def);
			if(str==null) {
				String gname = "get"+Character.toUpperCase(name.charAt(0))+name.substring(1);
				try {
					Method meth = bc.getMethod(gname, params);
					Object obj = meth.invoke(bean, args);
					str = obj==null?null:obj.toString(); // easy string convert... 
				} catch (Exception e) {
				}
			}
			// Try Properties loaded third
			if(str==null) {
				str = ca.getProperty(name,null);
			}
			
			// Take def if nothing else
			if(str==null) {
				str = def;
				// don't log defaults
			} else {
				str = str.trim(); // this is vital in Property File based values, as spaces can hide easily
			}
			// Note: Can't log during configuration
			return str;
		}
	}

	public static Get NULL = new Get() {
		public String get(String name, String def, boolean print) {
			return def;
		}
	};

	public static class AccessGet implements Get {
		private Access access;
		public AccessGet(Access access) {
			this.access = access;
		}
		public String get(String name, String def, boolean print) {
			String gotten = access.getProperty(name, def);
			if(print) {
				if(gotten == null) {
					access.log(Level.INIT,name, "is not set");
				} else {
					access.log(Level.INIT,name, "is set to", gotten);
				}
			}
			return gotten;
		}
	}

}
