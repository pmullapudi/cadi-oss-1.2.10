/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.dos;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.CadiException;
import com.att.cadi.Taf.LifeForm;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.PuntTafResp;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.TafResp.RESP;

public class DenialOfServiceTaf implements HttpTaf {
	private static Map<String, Counter> deniedIP=null, deniedID=null;
	private Access access;
	
	/**
	 * 
	 * @param hostname
	 * @param prod
	 * @throws CadiException
	 */
	public DenialOfServiceTaf(Access access) throws CadiException {
		this.access = access;
	}

	public TafResp validate(LifeForm reading, HttpServletRequest req, final HttpServletResponse resp) {
		// Performance, when not needed
		if(deniedIP != null) {
			String ip;
			Counter c = deniedIP.get(ip=req.getRemoteAddr());
			if(c!=null) {
				c.inc();
				return respDenyIP(access,ip);
			}
		}
		
		// Note:  Can't process Principal, because this is the first TAF, and no Principal is created.
		// Other TAFs use "isDenied()" on this Object to validate.
		return PuntTafResp.singleton();
	}

	public Resp revalidate(CachedPrincipal prin) {
		// We always return NOT MINE, because DOS Taf does not ever validate
		return Resp.NOT_MINE;
	}

	/*
	 *  for use in Other TAFs, before they attempt backend validation of 
	 */
	public static Counter isDeniedID(String identity) {
		if(deniedID!=null) {
			return deniedID.get(identity);
		}
		return null;
	}
	
	/**
	 *  
	 */
	public static Counter isDeniedIP(String ipvX) {
		if(deniedID!=null) {
			return deniedID.get(ipvX);
		}
		return null;
	}

	/**
	 * Return of "True" means IP has been added.
	 * Return of "False" means IP already added.
	 * 
	 * @param ip
	 * @return
	 */
	public static synchronized boolean denyIP(String ip) {
		if(deniedIP==null) {
			deniedIP = new HashMap<String,Counter>();
			deniedIP.put(ip, new Counter(ip)); // Noted duplicated for minimum time spent
			return true;
		}
		if(deniedIP.get(ip)==null) {
			deniedIP.put(ip, new Counter(ip));
			return true;
		}
		return false;
	}
	
	/**
	 * Return of "True" means IP has was removed.
	 * Return of "False" means IP wasn't being denied.
	 * 
	 * @param ip
	 * @return
	 */
	public static synchronized boolean removeDenyIP(String ip) {
		if(deniedIP==null) return false;
		if(deniedIP.remove(ip)!=null) {
			if(deniedIP.isEmpty())deniedIP=null; // go faster is no denials
			return true;
		}
		return false;
	}

	/**
	 * Return of "True" means ID has been added.
	 * Return of "False" means ID already added.
	 * 
	 * @param ip
	 * @return
	 */
	public static synchronized boolean denyID(String id) {
		if(deniedID==null) {
			deniedID = new HashMap<String,Counter>();
			deniedID.put(id, new Counter(id)); // Noted duplicated for minimum time spent
			return true;
		}
		if(deniedID.get(id)==null) {
			deniedID.put(id, new Counter(id));
			return true;
		}
		return false;
	}

	/**
	 * Return of "True" means ID has was removed.
	 * Return of "False" means ID wasn't being denied.
	 * 
	 * @param ip
	 * @return
	 */
	public static synchronized boolean removeDenyID(String id) {
		if(deniedID==null) return false;
		if(deniedID.remove(id)!=null) { 
			if(deniedID.isEmpty())deniedID=null;
			return true;
		}
		return false;
	}
	
	public List<String> report() {
		int initSize = 0;
		if(deniedIP!=null)initSize+=deniedIP.size();
		if(deniedID!=null)initSize+=deniedID.size();
		ArrayList<String> al = new ArrayList<String>(initSize);
		if(deniedID!=null) {
			for(Counter c : deniedID.values()) {
				al.add(c.toString());
			}
		}
		if(deniedIP!=null) {
			for(Counter c : deniedIP.values()) {
				al.add(c.toString());
			}
		}
		return al;
	}
	
	public static class Counter {
		private final String name; 
		private int count = 0;
		private Date first;
		private long last; // note, we use "last" as long, to avoid popping useless dates on Heap.
		
		public Counter(String name) {
			this.name = name;
			first = null;
			last = 0L;
			count = 0;
		}
		
		public String getName() {
			return name;
		}
		
		public int getCount() {
			return count;
		}

		public long getLast() {
			return last;
		}
		
		/*
		 * Only allow Denial of ServiceTaf to increment
		 */
		private synchronized void inc() {
			++count;
			last = System.currentTimeMillis();
			if(first==null) {
				first = new Date(last);
			}
		}
		
		public String toString() {
			if(count==0) 
				return name + " is on the denied list, but has not attempted Access"; 
			else 
				return 
					name +
					" has been denied " +
					count +
					" times since " +
					first +
					".  Last denial was " +
					new Date(last);
		}
	}

	public static TafResp respDenyID(Access access, String identity) {
		return new DenialOfServiceTafResp(access, RESP.NO_FURTHER_PROCESSING, identity + " is on the Indentity Denial list");
	}
	
	public static TafResp respDenyIP(Access access, String ip) {
		return new DenialOfServiceTafResp(access, RESP.NO_FURTHER_PROCESSING, ip + " is on the IP Denial list");
	}

}
