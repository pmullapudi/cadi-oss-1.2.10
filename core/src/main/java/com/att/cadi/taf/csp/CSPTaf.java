/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.csp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.CadiException;
import com.att.cadi.Taf;
import com.att.cadi.config.Config;
import com.att.cadi.taf.PuntTafResp;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.csp.CSPTafResp.Creator;

public class CSPTaf implements Taf {
	private static final String COMMA_WHITESPACE = "\\s*,\\s*";
	protected String hostname;
	//protected String address;
	protected String cspurl;
	protected String cspEnv;
	
	protected Access access;

	/**
	 * Obtain Hostname from standard InetAddress code.  Just tell whether Prod or Test environment.
	 * 
	 * @param isProd
	 * @throws CadiException 
	 * @throws UnknownHostException
	 */
	public CSPTaf(Access access, String cspEnv) throws CadiException {
		this(access, null, cspEnv);
	}
	
	
	/**
	 * 
	 * @param host

	 * @param isProd
	 * @throws CadiException
	 * @throws UnknownHostException 
	 */
	public CSPTaf(Access access, String host, String cspEnv) throws CadiException {
		this.access = access;
		this.cspEnv = cspEnv;
		try {
			hostname = host==null?InetAddress.getLocalHost().getHostName():host;
			//address = addr==null?InetAddress.getLocalHost().getHostAddress():addr;
		} catch (Exception e) {
			throw new CadiException(e);
		}
		// Pick the right CSP URL for this app based on Prod/Test flag and domain
		load(access.classLoader(), hostname,cspEnv);
		
		if(cspurl==null) {
			throw new CadiException("Can't get valid domain from " + hostname);
		}
	}
	
	public TafResp validate(LifeForm reading, String... info) {
		return reading == LifeForm.SBLF
				?PuntTafResp.singleton()
				:validate(CSPTafResp.creator, info);
	}
	
	protected TafResp validate(Creator creator, String ... info) {
		String attsesec = info.length>0?info[0]:null;
		String requestURL = info.length>1?info[1]:null;
		String atteshr = info.length>2?info[2]:null; // passing in as null is fine.
		
		if(attsesec==null) {
			return creator.create(
					access,
					null, // no Principal
					"No CSP Credential",
					requestURL==null?null:cspurl + "/?retURL=" + requestURL + "&sysName=" + hostname);
		}

		Principal pr = CSPPrincipal.extract(attsesec, cspEnv, atteshr);
		if(pr==null) {
			access.log(Level.INFO,"Principal cannot be derived from CSP Credential");
			return creator.create(
					access,
					null, // no principal
					"CSP credential exists, but is invalid for " + cspEnv,
					requestURL==null?null:cspurl + "/?retURL=" + requestURL + "&sysName=" + hostname);
		}
		return creator.create(access,pr,"Valid CSP credential for " + pr.getName(),"");
	}

	//10/18, Bill Suggested putting in file that's in the Jar... this allows Classpath overloading of the file without recompile
	private void load(ClassLoader cl, String hostname, String cspEnv) throws CadiException {
		Reader r = null;
		String filename = access.getProperty(Config.CSP_SYSTEMS_CONF_FILE, null);
		if(filename!=null) {
			try {
				r = new FileReader(filename);
			} catch (FileNotFoundException e) {
				access.log(Level.WARN,filename + ", set from", Config.CSP_SYSTEMS_CONF_FILE +", does not exist");
			}
		}
		if(r==null) {
			InputStream is = cl.getResourceAsStream(Config.CSP_SYSTEMS_CONF);
			if(is==null) {
				access.log(Level.WARN,"Cannot read CSPSystems.conf on Classpath or direct File.  Reverting to HardCoded List");
				r = new StringReader(
					"PROD,att.com,https://www.e-access.att.com/empsvcs/hrpinmgt/pagLogin\n"+
					"PROD,sbc.com,https://www.e-access.sbc.com/empsvcs/hrpinmgt/pagLogin\n"+
					"PROD,bls.com,https://www.e-access.bls.com/empsvcs/hrpinmgt/pagLogin\n"+
					"PROD,cingular.net,https://www.e-access.cingular.net/empsvcs/hrpinmgt/pagLogin\n"+
	
					"DEVL,att.com,https://webtest.csp.att.com/empsvcs/hrpinmgt/pagLogin\n"+
					"DEVL,sbctest.com,https://webtest.sbctest.com/empsvcs/hrpinmgt/pagLogin\n"+
					"DEVL,sbc.com,https://webtest.sbc.com/empsvcs/hrpinmgt/pagLogin\n"+
					"DEVL,bls.com,https://webtest.bls.com/empsvcs/hrpinmgt/pagLogin\n"+
					"DEVL,cingular.net,https://webtest.cingular.net/empsvcs/hrpinmgt/pagLogin\n");
			} else {
				r = new InputStreamReader(is);
			}
		}
		BufferedReader br = new BufferedReader(r);
		String line;
		try {
			while(cspurl == null && (line = br.readLine())!=null) {
				String[] elements = line.trim().split(COMMA_WHITESPACE);
				// This "if" will eliminate comments, blank lines, non-target systems and non-matching hostnames
				if(elements.length==3 && elements[0].equals(cspEnv) && hostname.endsWith(elements[1])) {
					cspurl = elements[2];
				}
			};
		} catch (IOException e) {
			throw new CadiException("Error reading " + Config.CSP_SYSTEMS_CONF,e);
		} finally {
			try {
				if(r!=null)r.close();
			} catch (IOException e) {
				access.log(e,"error while closing");
			}
		}
		
		if(cspurl==null)throw new CadiException("Can't get valid domain from " + hostname);
	}
	
	/**
	 * This function provided to test the environment in which it is placed via the "Test" class in 
	 * Default package
	 * @param domain
	 * @param prefix
	 * @return
	 */
	public static final String domainSupportedCSP(String domain, String prefix) {
		StringBuilder content = new StringBuilder();
		
		content.append(prefix);
		content.append("CSP Availability\n");

		boolean csp = false;
		InputStream is = ClassLoader.getSystemResourceAsStream(Config.CSP_SYSTEMS_CONF); 
		if(is==null) {
			content.append(prefix);
			content.append(prefix);
			content.append("!!WARNING!! ");
			content.append(Config.CSP_SYSTEMS_CONF);
			content.append(" must be on the classpath to use CSP Systems\n\n");
		} else {
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			try {
				while((line = br.readLine())!=null) {
					String[] elements = line.trim().split(COMMA_WHITESPACE);
					if(elements.length==3 && domain.endsWith(elements[1])) {
						content.append(prefix);
						content.append(prefix);
						content.append("CSP ");
						content.append(elements[0]);
						content.append(" supports the \"");
						content.append(elements[1]);
						content.append("\" domain\n");
						csp = true;
					}
				};
			} catch (IOException e) {
				content.append(prefix);
				content.append(prefix);
				content.append("!!ERROR!! ");
				content.append("Error reading ");
				content.append(Config.CSP_SYSTEMS_CONF);
				content.append("\n\n");
			} finally {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
		
		if(!csp) {
			content.append(prefix);
			content.append(prefix);
			content.append("CSP is not available for your machine domain");
		}
		return content.toString();
		
	}
	
	public String toString() {
		return "CSP TAF activated on " + cspEnv + " CSP Environment for " + hostname;
	}
}
