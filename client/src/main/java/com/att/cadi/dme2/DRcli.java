/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.dme2;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Exception;
import com.att.aft.dme2.api.DME2Manager;
import com.att.aft.dme2.jms.util.DME2UniformResource;
import com.att.aft.dme2.manager.registry.DME2Endpoint;
import com.att.aft.dme2.request.DmeUniformResource;
import com.att.cadi.CadiException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.client.EClient;
import com.att.cadi.client.Rcli;
import com.att.inno.env.APIException;
import com.att.inno.env.Data.TYPE;

/**
 * DME2 Rosetta Client
 * 
 * JAXB defined JSON or XML over DME2 middleware
 * 
 * @author jg1555
 *
 * @param <T>
 */
public class DRcli extends Rcli<DME2Client> {
	// Can be more efficient if tied to manager, apparently.  Can pass in null.
	DME2Manager manager=null;
	
	public static final SecuritySetter<DME2Client> NULL_SS = new SecuritySetter<DME2Client>() {
		@Override
		public void setSecurity(DME2Client client) {
		}
		
		@Override
		public String toString() {
			return "Null Security Setter";
		}
	};

	public DRcli(URI uri, SecuritySetter<DME2Client> secSet) {
		this.uri = uri;
		type = TYPE.JSON;
		apiVersion = null;
		ss=secSet;
	}
	
	@Override
	protected DRcli clone(URI uri, SecuritySetter<DME2Client> ss) {
		return new DRcli(uri,ss);
	}



	/**
	 * Note from Thaniga on 11/5.  DME2Client is not expected to be reused... need a fresh one
	 * on each transaction, which is expected to cover the Async aspects.
	 * 
	 * @return
	 * @throws APIException 
	 * @throws DME2Exception 
	 */
	protected EClient<DME2Client> client() throws CadiException {
		try {
			return new DEClient(manager,ss,uri,readTimeout);
		} catch (DME2Exception e) {
			throw new CadiException(e);
		}
	}

	public DRcli setManager(DME2Manager dme2Manager) {
		manager = dme2Manager;
		return this;
	}

	public List<DRcli> all() throws DME2Exception, APIException {
		ArrayList<DRcli> al = new ArrayList<DRcli>();
		
		if(manager == null) {
			manager = DME2Manager.getDefaultInstance();
		}
		try {
			//DME2Endpoint[] endp = manager.getEndpoints(new DME2UniformResource(uri));
			DME2Endpoint[] endp = manager.getEndpoints(new DmeUniformResource(manager.getConfig(),uri));
			// Convert Searchable Endpoints to Direct Endpoints
			for(DME2Endpoint de : endp) {
				al.add(new DRcli(
						new URI(uri.getScheme(),null,de.getHost(),de.getPort(),null,null,null),ss)
//						new URI(uri.getScheme(),null,de.getHost(),de.getPort(),uri.getPath(),null,null),ss)
				.setManager(manager)
				);
			}
		} catch (MalformedURLException e) {
			throw new APIException("Invalid URL",e);
		} catch (URISyntaxException e) {
			throw new APIException("Invalid URI",e);
		}
		return al;
	}

}
