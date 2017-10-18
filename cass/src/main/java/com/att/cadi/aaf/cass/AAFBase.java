/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.aaf.cass;

import java.io.InputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import org.apache.cassandra.auth.DataResource;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.exceptions.ConfigurationException;

import com.att.aft.dme2.api.DME2Exception;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.Lur;
import com.att.cadi.SLF4JAccess;
import com.att.cadi.aaf.v2_0.AAFAuthn;
import com.att.cadi.aaf.v2_0.AbsAAFLur;
import com.att.cadi.config.Config;
import com.att.cadi.config.Get;
import com.att.cadi.lur.EpiLur;
import com.att.cadi.lur.LocalLur;
import com.att.cadi.lur.aaf.AAFPermission;

public abstract class AAFBase {
	protected static final Set<IAuthenticator.Option> options;
	protected static final Set<DataResource> dataResource;

	static {
		options = new HashSet<IAuthenticator.Option>();
		options.add(IAuthenticator.Option.PASSWORD);
		
		dataResource = new HashSet<DataResource>();
		dataResource.add(DataResource.columnFamily("system_auth", "credentials"));
	}
	
	protected static Access access;
	protected static AAFAuthn aafAuthn;
	protected static LocalLur localLur;
	protected static AbsAAFLur<AAFPermission> aafLur;
	protected static String default_realm;
    protected static String cluster_name;
    protected static String perm_type;
	private static boolean props_ok = false;
	
	/**
	 * If you use your own Access Class, this must be called before 
	 * "setup()" is invoked by Cassandra.
	 * 
	 * Otherwise, it will default to reading Properties CADI style.
	 * 
	 * @param access
	 */
	public static void setAccess(Access access) {
		AAFBase.access = access;
	}
	
    public void validateConfiguration() throws ConfigurationException {
    	setup();
    	if(!props_ok) 
    		throw new ConfigurationException("AAF not initialized");
    }
    
	@SuppressWarnings("unchecked")
	public synchronized void setup() {
		if(aafAuthn == null) {
			try {
				if(access==null) {
					String value = System.getProperty(Config.CADI_PROP_FILES, "cadi.properties");
					Properties initial = new Properties();
					URL cadi_props = ClassLoader.getSystemResource(value);
					if(cadi_props!=null) {
						InputStream is = cadi_props.openStream();
						try {
							initial.load(is);
						} finally {
							is.close();
						}
					} else {
						System.out.println("No CADI_PROP files in ClassPath");
						initial.setProperty(Config.CADI_PROP_FILES, value);
					}
					access = new SLF4JAccess(initial);
				}
				props_ok = true;
				Get getter = new Get.AccessGet(access);
				if((perm_type = getter.get("cass_group_name",null,true))==null) {
					props_ok=false;
				} else {
					perm_type = perm_type + ".cass";
				}
				
				if((cluster_name = getter.get("cass_cluster_name",null,true))==null) {
					if((cluster_name = DatabaseDescriptor.getClusterName())==null) {
						props_ok=false;
					}
				}

				if((default_realm = getter.get(Config.AAF_DEFAULT_REALM, null,true))==null) {
					props_ok=false;
				}
				
				if(props_ok==false) {
					return;
				}

				// AAFLur has pool of DME clients as needed, and Caches Client lookups
				Lur lur = Config.configLur(getter, access);
				// Loop through to find AAFLur out of possible Lurs, to reuse AAFCon
				if(lur instanceof EpiLur) {
					EpiLur elur = (EpiLur)lur;
					for(int i=0; (lur = elur.get(i))!=null;++i) {
						if(lur instanceof AbsAAFLur) {
							aafLur=(AbsAAFLur<AAFPermission>)lur;
							aafAuthn = new AAFAuthn(aafLur.aaf,aafLur);
							break;
						} else if(lur instanceof LocalLur) {
							localLur = (LocalLur)lur;
						}
					}
				} else if(lur instanceof AbsAAFLur) {
					aafLur=(AbsAAFLur<AAFPermission>)lur;
					aafAuthn = new AAFAuthn(aafLur.aaf,aafLur);
				}
				if(aafAuthn==null) {
					access.log(Level.INIT,"Failed to instantiate full AAF access");
					props_ok = false;
				}
			} catch (DME2Exception e) {
				access.log(Level.WARN, "AAF not currently up, continuing");
			} catch (Exception e) {
				aafAuthn=null;
				if(access!=null)access.log(e, "Failed to initialize AAF");
				props_ok = false;
			}
		}		
	}

	public Set<DataResource> protectedResources() {
		access.log(Level.DEBUG, "Data Resource asked for: it's",dataResource.isEmpty()?"":"not","empty");
		return dataResource;
	}
	
	public Set<IAuthenticator.Option> supportedOptions() {
		access.log(Level.DEBUG, "supportedOptions() called");
		return options;
	}
	  
	public Set<IAuthenticator.Option> alterableOptions() {
		access.log(Level.DEBUG, "alterableOptions() called");
		return options;
	}


}
