/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.dme2;


import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.api.DME2Server;
import com.att.aft.dme2.manager.registry.DME2Endpoint;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.Locator;
import com.att.cadi.LocatorException;

public class DME2Locator implements Locator {
	private DME2Manager dm;
	private DME2Endpoint[] endpoints;
	private Access access;
	private String service;
	private String version;
	private String routeOffer;
	private String envContext;
	private String thisMachine;
	private String pathInfo;
	private int thisPort;
	private boolean removeSelf;
	private final static Random random = new Random();

	// Default is to not bother trying to remove self
	public DME2Locator(Access access, DME2Manager dm, String service, String version, String envContext, String routeOffer) throws DME2Exception, UnknownHostException, LocatorException {
		this(access,dm,service,version,envContext,routeOffer,false);
	}
	
	public DME2Locator(Access access, DME2Manager dm, String service, String version, String envContext, String routeOffer, boolean removeSelf) throws DME2Exception, UnknownHostException, LocatorException {
		this.access = access;
		if(dm==null) {
			this.dm = new DME2Manager("DME2Locator created DME2Manager",System.getProperties());
		} else {
			this.dm = dm;
		}
		this.service = service;
		this.version = version;
		this.envContext = envContext;
		this.routeOffer = routeOffer;
		refresh();
		DME2Server server = dm.getServer(); 
		if(server == null) {
			thisMachine = InetAddress.getLocalHost().getHostName();
			thisPort = 0;
		} else {
			try {
//				thisMachine = server.getHostname();
//				thisPort = server.getPort();
			} catch(NullPointerException np) { // BAD BOY, DME2...
				access.log(Level.ERROR, "WARNING: DME2 threw a NullPointer Exception getting Server Machine and Port");
				thisMachine = InetAddress.getLocalHost().getHostName();
				thisPort = 0;
			}
		}
		this.removeSelf = removeSelf;
	}

	// Default is to not bother trying to remove self
	public DME2Locator(Access access, DME2Manager dm, String aafurl) throws DME2Exception, UnknownHostException, LocatorException {
		this(access,dm,aafurl,false);
	}
	
	public DME2Locator(Access access, DME2Manager dm, String aafurl, boolean removeSelf) throws DME2Exception, UnknownHostException, LocatorException {
		if(aafurl==null) throw new LocatorException("URL is null");
		this.access = access;
		if(dm==null) {
			this.dm = new DME2Manager("DME2Locator created DME2Manager",System.getProperties());
		} else {
			this.dm = dm;
		}
		String[] split = aafurl.split("/");
		StringBuilder sb = new StringBuilder();
		boolean dme2Entered = false;
		for(String s : split) {
			if(s.startsWith(     "service=")) this.service = s.substring(8);
			else if(s.startsWith("version=")) this.version = s.substring(8);
			else if(s.startsWith("envContext=")) this.envContext = s.substring(11);
			else if(s.startsWith("routeOffer=")) {
				this.routeOffer = s.substring(11);
				dme2Entered = true;
			}
			else if(dme2Entered) {
				sb.append('/');
				sb.append(s);
			}
			pathInfo = sb.toString();
		}
		DME2Server server = dm.getServer(); 
		if(server == null) {
			thisMachine = InetAddress.getLocalHost().getHostName();
			thisPort = 0;
		} else {
//			thisMachine = server.getHostname();
			if(thisMachine==null) { // even if server !=null, apparently, it can be uninitialized
				thisMachine = InetAddress.getLocalHost().getHostName();
				thisPort = 0;
			} else {
//				thisPort = server.getPort();
			}			
		}
		this.removeSelf=removeSelf;
		refresh();
	}
	
	@Override
	public boolean refresh() {
		try {
			endpoints = dm.findEndpoints(service, version, envContext, routeOffer, true);
			if(removeSelf) {
				for(int i=0;i<endpoints.length;++i) {
					if(endpoints[i].getPort()==thisPort && endpoints[i].getHost().equals(thisMachine))
						endpoints[i]=null;
				}
			}
			return endpoints.length!=0;
		} catch (Exception e) {
			access.log(Level.ERROR, e.getMessage());
		}
		return false;
	}

	private String noEndpointsString() {
		StringBuilder sb = new StringBuilder("No DME2 Endpoints found for ");
		sb.append(service);
		sb.append('/');
		sb.append(version);
		sb.append('/');
		sb.append(envContext);
		sb.append('/');
		sb.append(routeOffer);
		return sb.toString();
	}

	@Override
	public URI get(Locator.Item item) throws URISyntaxException, LocatorException {
		if(!hasItems()) 
			throw new LocatorException(noEndpointsString());
		if(item == null) 
			return null;

		Item li = ((Item)item);
		// if URI has been created, use it
		if(li.uri!=null)return li.uri;
	
		// URI not created, create it
		if(li.idx<endpoints.length) {
			DME2Endpoint de = endpoints[li.idx];
			if(de!=null) {
				return li.uri=new URI(de.getProtocol(),null,de.getHost(),de.getPort(),pathInfo,null,null);
			}
		}
		return null;
	}
	
	@Override
	public boolean hasItems() {
		return endpoints!=null && endpoints.length>0;
	}

	@Override
	public void invalidate(Locator.Item item)throws URISyntaxException, LocatorException {
		if(item instanceof Item) {
			int idx = ((Item)item).idx;
			if(idx<endpoints.length) {
				DME2Endpoint uhoh = endpoints[idx]; // Sometimes, DME2Endpoint, at least on File system, returns bogus entries.
				endpoints[idx]=null;
				boolean noneLeft=true;
				for(int i=0;i<endpoints.length && noneLeft;++i) {
					noneLeft = endpoints[i]==null;
				}
				if(noneLeft && refresh()) { // make sure DME2 isn't giving us the same invalidated entry...
					for(int i=0;i<endpoints.length && noneLeft;++i) {
						DME2Endpoint ep = endpoints[i];
						if(ep != null && 
						   ep.getHost().equals(uhoh.getHost()) &&
						   ep.getPort()==uhoh.getPort()) {
							 endpoints[i]=null;
						}
					}
				}
				
			}
		}
	}

	public class Item implements Locator.Item {
		private final int idx;
		private URI uri;
		private Item(int i) {
			idx = i;
			uri = null;
		}
	}

	@Override
	public Item best() throws LocatorException {
		if(!hasItems()) // checks endpoints
			if(!refresh()) throw new LocatorException("No DME2 Endpoints Available");
		
		// Some endpoints in Array are null.  Need sub array of usable endpoints
		int usable[] = new int[endpoints.length];
		int count=0;
		for(int i=0;i<endpoints.length;++i) {
			if(endpoints[i]!=null) {
				usable[count++] = i;
			}
		}
		switch(count) {
			case 0: refresh(); return null;
			case 1: return new Item(usable[0]);
			default:
				int samemach[] = new int[count];
				int samecount = 0,closecount=0;
				// has to be sortable
				Integer closemach[] = new Integer[count];
				
				// Analyze for Same Machine or Remote machines
				for(int i=0;i<count;++i) {
					DME2Endpoint ep = endpoints[usable[i]];
					String host = ep.getHost();
					if(thisMachine.equalsIgnoreCase(host)) {
						samemach[samecount++] = usable[i];
					} else {
						closemach[closecount++] = usable[i];
					}
				}
				
				switch(samecount) {
					case 0: break;
					case 1: return new Item(samemach[0]);
					default: // return randomized is multiple Endpoints on local machine.
						int i = random.nextInt();
						return new Item(usable[Math.abs(i%samecount)]);
				}
				
				// Analyze for closest remote
				switch(closecount) {
					case 0:	return null;
					case 1: return new Item(closemach[0]);
					default: // return closest machine
						DoubIndex remote[] = new DoubIndex[closecount];
						int remotecount = 0;
						for(int i=0;i<closecount;++i) {
							DME2Endpoint de = endpoints[usable[i]];
							remote[remotecount++] = new DoubIndex(de.getDistance(),i);
						}
						Arrays.sort(remote,new Comparator<DoubIndex> () {
							@Override
							public int compare(DoubIndex a, DoubIndex b) {
								if(a.d<b.d) return -1;
								if(a.d>b.d) return 1;
								return (random.nextInt()%1)==0?1:0;// randomize if the same
							}
							
						});
						return new Item(remote[0].idx);
				}
		}
	}
	
	private class DoubIndex {
		public final double d;
		public final int idx;
		
		public DoubIndex(double doub, int i) {
			d = doub;
			idx = i;
		}
	}
	@Override
	public Item first() {
		if(endpoints==null)return null;
		for(int i=0;i<endpoints.length;++i) {
			if(endpoints[i]!=null)
				return new Item(i); 
		}
		return null;
	}

	@Override
	public Item next(Locator.Item item) throws LocatorException {
		if(endpoints==null || endpoints.length==0 || !(item instanceof Item))return null;
		int idx = ((Item)item).idx +1;
		for(int i=idx;i<endpoints.length;++i) {
			if(endpoints[i]!=null)
				return new Item(i); 
		}
// This is a mistake..  will start infinite loops
//		// Did not have any at end... try beginning
//		for(int i=0;i<idx-1;++i) {
//			if(endpoints[i]!=null)
//				return new Item(i); 
//		}
//		// If still nothing, refresh
//		refresh();
		return null;
	}
}
