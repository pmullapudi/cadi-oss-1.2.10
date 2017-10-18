/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.client.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.util.Properties;

import com.att.aft.dme2.api.DME2Manager;
import com.att.cadi.Access;
import com.att.cadi.Locator;
import com.att.cadi.Access.Level;
import com.att.cadi.Locator.Item;
import com.att.cadi.dme2.DME2Locator;

public class PaulUzee {
	public static void main(String[] args) {
		try {
			// You'll want to put this on Command line "-D" probably
			Properties props = System.getProperties();
			props.put("AFT_LATITUDE","32.780140");
			props.put("AFT_LONGITUDE","-96.800451");
			props.put("AFT_ENVIRONMENT","AFTPRD");

			//
			// Use an "Access" class to hook up logging, properties, etc.
			// Make one that ties into your code's logging, property mechanism, etc.
			//
			Access access = new PaulAccess();
			
			DME2Manager dm = new DME2Manager("Paul Uzee's Test",props);
			Locator loc = new DME2Locator(access ,dm,"com.att.authz.AuthorizationService","2.0","PROD","DEFAULT");

			
			for(Item item = loc.first(); item!=null; item=loc.next(item)) {
				URI location = loc.get(item);
				access.log(Level.INFO,location);
				access.log(Level.INFO,location.getScheme());
				access.log(Level.INFO,location.getHost());
				access.log(Level.INFO, location.getPort());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
			
	}

	private static class PaulAccess implements Access {
		private Level willWrite = Level.INFO;

		@Override
		public ClassLoader classLoader() {
			return getClass().getClassLoader();
		}

		@Override
		public String decrypt(String data, boolean def) throws IOException {
			return data;
		}

		@Override
		public String getProperty(String tag, String def) {
			return System.getProperty(tag, def);
		}

		@Override
		public void load(InputStream is) throws IOException {
			System.getProperties().load(is);
		}

		@Override
		public void log(Level level, Object... obj) {
			if(level.compareTo(willWrite)<0) return;
			PrintStream ps;
			switch(level) {
				case DEBUG:
				case AUDIT:
				case ERROR:
				case WARN:
					ps = System.err;
					break;
				case INFO:
				case INIT:
				default:
					ps = System.out;
			}
			boolean first = true;
			for(Object o : obj) {
				if(first)first=false;
				else ps.print(' ');
				ps.print(o.toString());
			}
			ps.println();
		}

		@Override
		public void log(Exception e, Object... obj) {
			Object[] objs = new Object[obj.length+1];
			objs[0]=e.getMessage();
			System.arraycopy(objs, 1, obj, 0, obj.length);
			log(Level.ERROR,e,objs);
		}

		@Override
		public void setLogLevel(Level l) {
			willWrite = l;
		}
	};
}
