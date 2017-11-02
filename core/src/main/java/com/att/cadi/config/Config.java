/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.att.cadi.AbsUserCache;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.CachingLur;
import com.att.cadi.CadiException;
import com.att.cadi.CredVal;
import com.att.cadi.Locator;
import com.att.cadi.Lur;
import com.att.cadi.Symm;
import com.att.cadi.filter.CadiFilter;
import com.att.cadi.lur.EpiLur;
import com.att.cadi.lur.LocalLur;
import com.att.cadi.lur.NullLur;
import com.att.cadi.taf.HttpEpiTaf;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.basic.BasicHttpTaf;
import com.att.cadi.taf.cert.CertIdentity;
import com.att.cadi.taf.cert.X509Taf;
import com.att.cadi.taf.csp.CSPHttpTaf;
import com.att.cadi.taf.dos.DenialOfServiceTaf;
import com.att.cadi.taf.localhost.LocalhostTaf;

/**
 * Create a Consistent Configuration mechanism, even when configuration styles are as vastly different as
 * Properties vs JavaBeans vs FilterConfigs...
 * 
 *
 */
public class Config {
	public static final String UTF_8 = "UTF-8";

	// Property Names associated with configurations.
	// As of 1.0.2, these have had the dots removed so as to be compatible with JavaBean style
	// configurations as well as property list style.
	public static final String HOSTNAME = "hostname";
	public static final String CADI_PROP_FILES = "cadi_prop_files"; // Additional Properties files (separate with ;)
	public static final String CADI_LOGLEVEL = "cadi_loglevel";
	public static final String CADI_KEYFILE = "cadi_keyfile";
	public static final String CADI_KEYSTORE = "cadi_keystore";
	public static final String CADI_KEYSTORE_PASSWORD = "cadi_keystore_password";
	public static final String CADI_ALIAS = "cadi_alias";
	public static final String CADI_LOGINPAGE_URL = "cadi_loginpage_url";

	public static final String CADI_KEY_PASSWORD = "cadi_key_password";
	public static final String CADI_TRUSTSTORE = "cadi_truststore";
	public static final String CADI_TRUSTSTORE_PASSWORD = "cadi_truststore_password";
	public static final String CADI_TRUST_ALL_X509 = "cadi_trust_all_x509";
	public static final String CADI_NOAUTHN = "cadi_noauthn";
	public static final String CADI_AS_USER = "AS_USER";
	public static final String CADI_LOC_LIST = "cadi_loc_list";
	
	public static final String CSP_DOMAIN = "csp_domain";
	public static final String CSP_HOSTNAME = "csp_hostname";
	public static final String CSP_DEVL_LOCALHOST = "csp_devl_localhost";
	public static final String CSP_USER_HEADER = "CSP_USER";
	public static final String CSP_SYSTEMS_CONF = "CSPSystems.conf";
    public static final String CSP_SYSTEMS_CONF_FILE = "csp_systems_conf_file";


	public static final String TGUARD_ENV="tguard_env";
	public static final String TGUARD_DOMAIN = "tguard_domain";
	public static final String TGUARD_TIMEOUT = "tguard_timeout";
	public static final String TGUARD_TIMEOUT_DEF = "5000";
	public static final String TGUARD_CERTS = "tguard_certs"; // comma delimited SHA-256 finger prints
//	public static final String TGUARD_DEVL_LOCALHOST = "tguard_devl_localhost";
//	public static final String TGUARD_USER_HEADER = "TGUARD_USER";

	public static final String LOCALHOST_ALLOW = "localhost_allow";
	public static final String LOCALHOST_DENY = "localhost_deny";
	
	public static final String BASIC_REALM = "basic_realm";  // what is sent to the client 
	public static final String BASIC_WARN = "basic_warn";  // Warning of insecure channel 
	public static final String USERS = "local_users";
	public static final String GROUPS = "local_groups";
	public static final String WRITE_TO = "local_writeto"; // dump RBAC to local file in Tomcat Style (some apps use)
	
	public static final String AAF_URL = "aaf_url"; //URL for AAF... Use to trigger AAF configuration
	public static final String AAF_MECHID = "aaf_id";
	public static final String AAF_MECHPASS = "aaf_password";
	public static final String AAF_LUR_CLASS = "aaf_lur_class";
	public static final String AAF_TAF_CLASS = "aaf_taf_class";
	public static final String AAF_CONN_TIMEOUT = "aaf_conn_timeout";
	public static final String AAF_CONN_TIMEOUT_DEF = "3000";
	public static final String AAF_READ_TIMEOUT = "aaf_timeout";
	public static final String AAF_READ_TIMEOUT_DEF = "5000";
	public static final String AAF_USER_EXPIRES = "aaf_user_expires";
	public static final String AAF_USER_EXPIRES_DEF = "600000"; // Default is 10 mins
	public static final String AAF_CLEAN_INTERVAL = "aaf_clean_interval";
	public static final String AAF_CLEAN_INTERVAL_DEF = "30000"; // Default is 30 seconds
	public static final String AAF_REFRESH_TRIGGER_COUNT = "aaf_refresh_trigger_count";
	public static final String AAF_REFRESH_TRIGGER_COUNT_DEF = "3"; // Default is 10 mins
	
	public static final String AAF_HIGH_COUNT = "aaf_high_count";
	public static final String AAF_HIGH_COUNT_DEF = "1000"; // Default is 1000 entries
	public static final String AAF_PERM_MAP = "aaf_perm_map";
	public static final String AAF_DEPLOYED_VERSION = "DEPLOYED_VERSION";
	public static final String AAF_CERT_IDS = "aaf_cert_ids";
	public static final String AAF_DEBUG_IDS = "aaf_debug_ids"; // comma delimited

	public static final String PATHFILTER_URLPATTERN = "pathfilter_urlpattern";
	public static final String PATHFILTER_STACK = "pathfilter_stack";
	public static final String PATHFILTER_NS = "pathfilter_ns";
	public static final String PATHFILTER_NOT_AUTHORIZED_MSG = "pathfilter_not_authorized_msg";

	
	public static final String AFT_DME2_TRUSTSTORE_PASSWORD = "AFT_DME2_TRUSTSTORE_PASSWORD";
	public static final String AFT_DME2_TRUSTSTORE = "AFT_DME2_TRUSTSTORE";
	public static final String AFT_DME2_KEYSTORE_PASSWORD = "AFT_DME2_KEYSTORE_PASSWORD";
	public static final String AFT_DME2_KEYSTORE = "AFT_DME2_KEYSTORE";
	public static final String AFT_DME2_SSL_TRUST_ALL = "AFT_DME2_SSL_TRUST_ALL";
	public static final String AFT_DME2_CLIENT_SSL_CERT_ALIAS = "AFT_DME2_CLIENT_SSL_CERT_ALIAS"; 

	
	// This one should go unpublic
	public static final String AAF_DEFAULT_REALM = "aaf_default_realm";
	private static String defaultRealm="none";
	public static final String AAF_DOMAIN_SUPPORT = "aaf_domain_support";
	public static final String AAF_DOMAIN_SUPPORT_DEF = ".com";

	public static void configPropFiles(Get getter, Access access) throws CadiException {
		String pf;
		if((pf = getter.get(CADI_PROP_FILES, null,true))==null) {
			access.log(Level.INIT, "There is no CADI Property File Property set (",CADI_PROP_FILES,")");
		} else {
			for(String str : pf.split(";")) {
				InputStream is = access.classLoader().getResourceAsStream(str);
				if(is==null) {
					File file = new File(str);
					access.log(Level.INIT, "Cadi Property File \"",str,"\" loading from file:",file.getAbsolutePath());
					if(file.exists()) {
						access.log(Level.INIT, "Cadi Property File is of length",file.length());
						try {
							FileInputStream fis = new FileInputStream(file);						
							try {
								access.load(fis);
							} finally {
								fis.close();
							}
						} catch (IOException e) {
							access.log(e);
							throw new CadiException(e);
						}
					} else {
						// Put in System Err too, because Logging may not be enabled quite yet
						String msg = "Cadi Property File: \"" + file.getAbsolutePath() + "\" does not exist!";
									 
						access.log(Level.ERROR, msg);
						throw new CadiException(msg);
					}
				} else {
					access.log(Level.INIT, "Cadi Property File \"",str,"\" loading from Classpath");
					try {
						try {
							access.load(is);
						} finally {
							is.close();
						}
					} catch (IOException e) {
						throw new CadiException(e);
					}
				}
			}
		}
			
		try {
			boolean hasCSP = getter.get(Config.CSP_DOMAIN, null,true)!=null;
			defaultRealm = getter.get(Config.AAF_DEFAULT_REALM,
					hasCSP?"csp.att.com":
					getter.get(Config.BASIC_REALM,
						getter.get(HOSTNAME, 
								InetAddress.getLocalHost().getHostName(),
								true),
						true),
					true);
		} catch (UnknownHostException e) {
			defaultRealm="none";
		}

		
		// Look for and load Discovery/DME2 Properties into System Properties, if exists
		for(String[] str : new String[][] {
				{"AFT_LATITUDE",null},
				{"AFT_LONGITUDE",null},
				{"AFT_ENVIRONMENT",null},
				{"SCLD_PLATFORM",null},
				{AFT_DME2_KEYSTORE,CADI_KEYSTORE},
				{AFT_DME2_KEYSTORE_PASSWORD,CADI_KEYSTORE_PASSWORD},
				{AFT_DME2_TRUSTSTORE,CADI_TRUSTSTORE},
				{AFT_DME2_TRUSTSTORE_PASSWORD,CADI_TRUSTSTORE_PASSWORD},
				{AFT_DME2_CLIENT_SSL_CERT_ALIAS,CADI_ALIAS},
				{"DME2_EP_REGISTRY_CLASS",null},// for Developer local access
				{"AFT_DME2_EP_REGISTRY_FS_DIR",null},
				{"DME2.DEBUG",null},
				{"AFT_DME2_HTTP_EXCHANGE_TRACE_ON",null},
				{"AFT_DME2_HOSTNAME",HOSTNAME}
				}) {
			String value = System.getProperty(str[0]); 
			if(value==null) {
				String key = str[1]==null?str[0]:str[1];
				value = getter.get(key, null, false);
				if(value!=null) {
					System.setProperty(str[0], value);
				}
			}
		}
		
//		/////////////////////////////////////////////////////
//		// Obtain the Local Encryption info
//		/////////////////////////////////////////////////////
//		Symm symm = null; // passwords encrypted with this... Wait to use until 
//		try {
//			String keyfile = getter.get(KEYFILE,null, true);
//			if(keyfile!=null) {
//				symm = decryptor(access, keyfile);
//			}
//		} catch(Exception e) {
//			throw new CadiException(e);
//		}
	}

	public static HttpTaf configHttpTaf(Access access, Get getter, CredVal up, Lur lur, Object ... additionalTafLurs) throws CadiException {
		HttpTaf taf;
		// Setup Host, in case Network reports an unusable Hostname (i.e. VTiers, VPNs, etc)
		String hostname = getter.get(HOSTNAME,null, true);
		if(hostname==null)
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (UnknownHostException e1) {
				throw new CadiException("Unable to determine Hostname",e1);
			}
		access.log(Level.INIT, "Hostname set to",hostname);
		// Get appropriate TAFs
		ArrayList<HttpTaf> htlist = new ArrayList<HttpTaf>();

		/////////////////////////////////////////////////////
		// Add a Denial of Service TAF
		// Note: how IPs and IDs are added are up to service type.
		// They call "DenialOfServiceTaf.denyIP(String) or denyID(String)
		/////////////////////////////////////////////////////
		htlist.add(new DenialOfServiceTaf(access));

		/////////////////////////////////////////////////////
		// Configure LocalHost
		/////////////////////////////////////////////////////
		//    allow, meaning treat as Validated (This is admittedly weak.  It just means someone has access to box)
		boolean localhost_allow = "TRUE".equalsIgnoreCase(getter.get(LOCALHOST_ALLOW,"FALSE",true));
		//    deny, meaning we can deny any access from local host (127.0.0.1, ::1, etc)
		boolean localhost_deny = "TRUE".equalsIgnoreCase(getter.get(LOCALHOST_DENY,"FALSE",true));
		// if at least one is true, install LocalHostTAF.  If both are false, it's not worth the CPU, skip it
		// Localhost (local box access)
		if(localhost_allow || localhost_deny) { 
			// log("Localhost Authorization is "); // already logged
			htlist.add(new LocalhostTaf(access,localhost_allow,localhost_deny));
			if(localhost_allow)  {
				access.log(Level.INIT,"WARNING! Localhost Authentication is enabled.  This is ONLY to be used in a Development setting, never Test or Production");
			}
			if(localhost_deny)  {
				access.log(Level.INIT,"Localhost Access to this service is disabled.");
			}

		} else {
			access.log(Level.INIT,"Localhost Authorization is disabled");
		}
		
	/////////////////////////////////////////////////////
	// Setup AAFCon for any following
	/////////////////////////////////////////////////////
		String aafURL = getter.get(AAF_URL,null,false);
		Object aafcon = null;
		Class<?> aafConClass = null;

		if(aafURL!=null) {
			try {
				try {
					aafConClass = access.classLoader().loadClass("com.att.cadi.aaf.v2_0.AAFCon");
				} catch (ClassNotFoundException cnfe) {
					aafConClass = access.getClass().getClassLoader().loadClass("com.att.cadi.aaf.v2_0.AAFCon");
				}
				
				Field f = null;
				if(access instanceof CadiFilter) {
					try {
						f = lur.getClass().getField("aaf");
						aafcon = f.get(lur);
					} catch (NoSuchFieldException nsfe) {
					}
				}

				Constructor<?> cstr = aafConClass.getConstructor(Access.class);
				if(cstr!=null) {
					if(aafcon == null) {
//						access.log(Level.INIT,"create aafcon");
						aafcon = cstr.newInstance(access);
						String mechid = getter.get(Config.AAF_MECHID, null, true);
						String pass = getter.get(Config.AAF_MECHPASS, null, false);
						if(mechid!=null && pass!=null) {
							try {
								try  {
									pass = access.decrypt(pass, false);
								} catch(Exception e) {
									access.log(e, "Cannot decypt password");
									throw e;
								}
								Method basicAuth = aafConClass.getMethod("basicAuth", String.class, String.class);
								basicAuth.invoke(aafcon, mechid,pass);
							} catch (NoSuchMethodException nsme) {
								// it's ok, don't use
							}
						}
					}
				}
			} catch (Exception e) {
				access.log(e,"AAF Connector could not be constructed with given Constructors.");
			}
		}


		/////////////////////////////////////////////////////
		// Configure CSP... (Global Sign On)
		/////////////////////////////////////////////////////
		// CSP Environment, translated to any other remote Environment validation mechanism...
		String csp_domain = getter.get(CSP_DOMAIN,null,true);

		if(csp_domain!=null) {
			String csp_hostname = getter.get(CSP_HOSTNAME, hostname,true);
			// We normally deny incoming Browser Localhost requests, because it causes CSP to loop.  Sometimes infinitely.
			// However, we must allow a Developer to do this when they follow CSP directions, and add entry into
			// the Hosts file, i.e. :   127.0.0.1 <mymachine>.att.com
			boolean csp_devl_localhost = "TRUE".equalsIgnoreCase(getter.get(CSP_DEVL_LOCALHOST,"FALSE",true));
			if(csp_devl_localhost)access.log(Access.Level.INIT, 
					"WARNING! Developer access to LocalHost for CSP is turned on.  Adjust your /etc/hosts per CSP requirements.");

			
			if(csp_hostname==null)csp_hostname=hostname;
			access.log(Level.INIT,"CSP Authorization is enabled on",csp_hostname,"on the",csp_domain," CSP Domain");
			htlist.add(new CSPHttpTaf(
				access,
				csp_hostname,
				csp_domain,
				csp_devl_localhost));
		} else {
			access.log(Level.INIT,"CSP Authorization is disabled.  Enable by setting csp_domain=\"PROD\" or \"DEVL\"");
		}

		
		/////////////////////////////////////////////////////
		// Configure Basic Auth (local content)
		/////////////////////////////////////////////////////
		String basic_realm = getter.get(BASIC_REALM,null,true);
		boolean basic_warn = "TRUE".equals(getter.get(BASIC_WARN,"FALSE",false));
		if(basic_realm!=null && up!=null) {
			access.log(Level.INIT,"Basic Authorization is enabled using realm",basic_realm);
			// Allow warning about insecure channel to be turned off
			if(!basic_warn)access.log(Level.INIT,"WARNING! The basic_warn property has been set to false.",
					" There will be no additional warning if Basic Auth is used on an insecure channel"
					);
			String aafCleanup = getter.get(AAF_USER_EXPIRES,AAF_USER_EXPIRES_DEF,true); // Default is 10 mins
			long userExp = Long.parseLong(aafCleanup);

			htlist.add(new BasicHttpTaf(access, up, basic_realm, userExp, basic_warn));
		} else {
			access.log(Level.INIT,"Local Basic Authorization is disabled.  Enable by setting basic_realm=<appropriate realm, i.e. my.att.com>");
		}
		
		/////////////////////////////////////////////////////
		// Configure AAF Driven Basic Auth
		/////////////////////////////////////////////////////
		Object aaftaf=null;
		if(aafcon==null) {
			access.log(Level.INIT,"AAF Connection (AAFcon) is null.  Cannot create an AAF TAF");
		} else if(aafURL==null) {
			access.log(Level.INIT,"No AAF URL in properties, Cannot create an AAF TAF");
		} else {// There's an AAF_URL... try to configure an AAF 
			String defName = aafURL.contains("version=2.0")?"com.att.cadi.aaf.v2_0.AAFTaf":"";
			String aafTafClass = getter.get(AAF_TAF_CLASS,defName,true);
			// Only 2.0 available at this time
			if("com.att.cadi.aaf.v2_0.AAFTaf".equals(aafTafClass)) { 
				aafConClass = aafcon.getClass();
				try {
					Class<?> aafClass = aafConClass.getClassLoader().loadClass(aafTafClass);
					Constructor<?> cstr = aafClass.getConstructor(aafConClass,boolean.class);
					if(cstr!=null) {
						aaftaf = cstr.newInstance(aafcon,basic_warn);
						if(aaftaf==null) {
							access.log(Level.INIT,"ERROR! AAF TAF Failed construction.  NOT Configured");
						} else {
							access.log(Level.INIT,"AAF TAF Configured to ",aafURL);
							// Note: will add later, after all others configured
						}
					}
				} catch(Exception e) {
					access.log(Level.INIT,"ERROR! AAF TAF Failed construction.  NOT Configured");
				}
			}
		}
		
		
		String alias = getter.get(CADI_ALIAS,null, true);

		/////////////////////////////////////////////////////
		// Configure tGuard... (AT&T Client Repo)
		/////////////////////////////////////////////////////
		// TGUARD Environment, translated to any other remote Environment validation mechanism...
		String tGuard_domain = getter.get(TGUARD_DOMAIN,null,true);
		String tGuard_env = getter.get(TGUARD_ENV, null,true);

		if(!("PROD".equals(tGuard_env) || "STAGE".equals(tGuard_env))) {
			access.log(Level.INIT, "tGuard Authorization is disabled.  Enable by setting", TGUARD_ENV, "to \"PROD\" or \"STAGE\"");
		} else if(tGuard_domain==null) {
			access.log(Level.INIT,TGUARD_DOMAIN + " must be set:  tGuard Authorization is disabled.");
		} else if(alias == null) {
			access.log(Level.INIT,CADI_ALIAS + " must be set:  tGuard Authorization is disabled.");
		} else {
			try {
				@SuppressWarnings("unchecked")
				Class<HttpTaf> tGuardClass = (Class<HttpTaf>)access.classLoader().loadClass("com.att.cadi.tguard.TGuardHttpTaf");
				if(aaftaf!=null) {
					Constructor<HttpTaf> tGuardCnst = tGuardClass.getConstructor(new Class[]{Access.class, AbsUserCache.class});
					htlist.add(tGuardCnst.newInstance(new Object[] {access,aaftaf}));
					access.log(Level.INIT,"tGuard Authorization is enabled on",tGuard_env,"on the",tGuard_domain," tGuard Domain");
				} else {
					Constructor<HttpTaf> tGuardCnst = tGuardClass.getConstructor(new Class[]{Access.class, int.class, int.class});
					htlist.add(tGuardCnst.newInstance(new Object[] {
							access,
							Integer.parseInt(getter.get(AAF_CLEAN_INTERVAL,AAF_CLEAN_INTERVAL_DEF,true)),
							Integer.parseInt(getter.get(AAF_HIGH_COUNT, AAF_HIGH_COUNT_DEF,true))
							}));
					access.log(Level.INIT,"tGuard Authorization is enabled on",tGuard_env,"on the",tGuard_domain," tGuard Domain");
				}
			} catch(ClassNotFoundException e) {
				access.log(Level.INIT,"tGuard Class not found:  tGuard Authorization is disabled.  Enable by adding cadi-tguard<version>.jar to classpath");
			} catch(Exception e) {
				access.log(e, Level.INIT,"tGuard Class cannot be loaded:  tGuard Authorization is disabled.");
			}
		}
		
		/////////////////////////////////////////////////////
		// Adding BasicAuth (AAF) last, after other primary Cookie Based
		// Needs to be before Cert... see below
		/////////////////////////////////////////////////////
		if(aaftaf!=null)
			htlist.add((HttpTaf)aaftaf);


		/////////////////////////////////////////////////////
		// Configure Client Cert TAF
		// Note: Needs to be after Basic Auth, because otherwise, 
		//       a Mutual SSL connection might give wrong Authentication
		/////////////////////////////////////////////////////
		String truststore = getter.get(CADI_TRUSTSTORE, getter.get("AFT_DME2_TRUSTSTORE", null, false), true);
		if(truststore!=null) {
			String truststore_pwd = getter.get(CADI_TRUSTSTORE_PASSWORD, getter.get("AFT_DME2_TRUSTSTORE_PASSWORD",null,false), true);
			if(truststore_pwd!=null) {
				if(truststore_pwd.startsWith(Symm.ENC)) {
					try {
						truststore_pwd = access.decrypt(truststore_pwd,false);
					} catch (IOException e) {
						throw new CadiException(CADI_TRUSTSTORE_PASSWORD + " cannot be decrypted",e);
					}
				}
				try {
					if(aafcon!=null) {
						Class<?> cls = ClassLoader.getSystemClassLoader().loadClass("com.att.cadi.aaf.cert.AAFListedCertIdentity");
						@SuppressWarnings("unchecked")
						Constructor<CertIdentity> cnst = (Constructor<CertIdentity>)cls.getConstructor(Access.class, aafConClass);
						
						htlist.add(new X509Taf(access,lur,cnst.newInstance(access,aafcon)));
						access.log(Level.INIT,"Certificate Authorization enabled with AAF Cert Identity");
					} else {
//						htlist.add(new X509Taf(access,lur));
//						access.log(Level.INIT,"Certificate Authorization enabled");
					}
				} catch (ClassNotFoundException e) {
					access.log(Level.INIT,"Certificate Authorization requires AAF Jars. It is now disabled",e);
				} catch (NoSuchMethodException e) {
					access.log(Level.INIT,"Certificate Authorization requires AAFListedCertIdentity. It is now disabled",e);
				} catch (SecurityException e) {
					access.log(Level.INIT,"AAFListedCertIdentity cannot be instantiated. Certificate Authorization is now disabled",e);
				} catch (InstantiationException e) {
					access.log(Level.INIT,"AAFListedCertIdentity cannot be instantiated. Certificate Authorization is now disabled",e);
				} catch (IllegalArgumentException e) {
					access.log(Level.INIT,"AAFListedCertIdentity cannot be instantiated. Certificate Authorization is now disabled",e);
				} catch (InvocationTargetException e) {
					access.log(Level.INIT,"AAFListedCertIdentity cannot be instantiated. Certificate Authorization is now disabled",e);
				} catch (IllegalAccessException e) {
					access.log(Level.INIT,"Certificate Authorization requires AAFListedCertIdentity. It is now disabled",e);
				} catch (CertificateException e) {
					access.log(Level.INIT,"Certificate Authorization failed, it is disabled",e);
				} catch (NoSuchAlgorithmException e) {
					access.log(Level.INIT,"Certificate Authorization failed, wrong Security Algorithm",e);
				}
			}
		} else {
			access.log(Level.INIT,"Certificate Authorization not enabled");

		}


		/////////////////////////////////////////////////////
		// Any Additional Lurs passed in Constructor
		/////////////////////////////////////////////////////
		for(Object additional : additionalTafLurs) {
			if(additional instanceof HttpTaf) {
				htlist.add((HttpTaf)additional);
				access.log(Level.INIT,additional);
			}
		}

		/////////////////////////////////////////////////////
		// Create EpiTaf from configured TAFs
		/////////////////////////////////////////////////////
		if(htlist.size()==1) {
			// just return the one
			taf = htlist.get(0);
		} else {
			HttpTaf[] htarray = new HttpTaf[htlist.size()];
			htlist.toArray(htarray);
			Locator locator = null;
			String loginPageURL = getter.get(CADI_LOGINPAGE_URL, null, true);
			if(loginPageURL!=null) {
				//TODO we can do direct URL too.
				if(loginPageURL.contains("DME2RESOLVE")) {
					try {
						Class<?> cls = ClassLoader.getSystemClassLoader().loadClass("com.att.cadi.dme2.DME2Locator");
						Class<?> dmcls = ClassLoader.getSystemClassLoader().loadClass("com.att.aft.dme2.api.DME2Manager");
						Constructor<?> cnst = cls.getConstructor(new Class[] {Access.class,dmcls,String.class});
						locator = (Locator)cnst.newInstance(new Object[] {access,null,loginPageURL});
					} catch (Exception e) {
						access.log(Level.INIT,"AAF Login Page accessed by " + loginPageURL + " requires DME2. It is now disabled",e);
					}
				} else {
					try {
						Class<?> cls = ClassLoader.getSystemClassLoader().loadClass("com.att.cadi.client.PropertyLocator");
						Constructor<?> cnst = cls.getConstructor(new Class[] {String.class});
						locator = (Locator)cnst.newInstance(new Object[] {loginPageURL});
					} catch (Exception e) {
						access.log(Level.INIT,"AAF Login Page accessed by " + loginPageURL + " requires PropertyLocator. It is now disabled",e);
					}
				}
			}
			taf = new HttpEpiTaf(access,locator, htarray); // ok to pass locator == null
			String level = getter.get(CADI_LOGLEVEL, null, true);
			if(level!=null) {
				access.setLogLevel(Level.valueOf(level));
			}
		}
		
		return taf;
	}
	
	public static Lur configLur(Get getter, Access access, Object ... additionalTafLurs) throws CadiException {
		List<Lur> lurs = new ArrayList<Lur>();
		
		/////////////////////////////////////////////////////
		// Configure a Local Property Based RBAC/LUR
		/////////////////////////////////////////////////////
		try {
			String users = getter.get(USERS,null,false);
			String groups = getter.get(GROUPS,null,false);

			if(groups!=null || users!=null) {
				LocalLur ll;
				lurs.add(ll = new LocalLur(access, users, groups)); // note b64==null is ok.. just means no encryption.
				
				String writeto = getter.get(WRITE_TO,null,false);
				if(writeto!=null) {
					String msg = UsersDump.updateUsers(writeto, ll);
					if(msg!=null) access.log(Level.INIT,"ERROR! Error Updating ",writeto,"with roles and users:",msg);
				}
			}
		} catch (IOException e) {
			throw new CadiException(e);
		}
		
		/////////////////////////////////////////////////////
		// Configure the AAF Lur (if any)
		/////////////////////////////////////////////////////
		String aafURL = getter.get(AAF_URL,null,false); // Trigger Property
			
		if(aafURL==null) {
			access.log(Level.INIT,"No AAF LUR properties, AAF will not be loaded");
		} else {// There's an AAF_URL... try to configure an AAF
			String defName = aafURL.contains("version=2.0")?"com.att.cadi.aaf.v2_0.AAFLurPerm":"com.att.cadi.lur.aaf.AAFLurPerm1_0";
			String aafLurClass = getter.get(AAF_LUR_CLASS,defName,true);
			////////////AAF Lur 2.0 /////////////
			if(aafLurClass.startsWith("com.att.cadi.aaf.v2_0")) { 
				try {
					Class<?> aafConClass;
					try {
						aafConClass = access.classLoader().loadClass("com.att.cadi.aaf.v2_0.AAFCon");
					} catch (ClassNotFoundException cnfe) {
						aafConClass = access.getClass().getClassLoader().loadClass("com.att.cadi.aaf.v2_0.AAFCon");
					}
					Constructor<?> cstr = aafConClass.getConstructor(Access.class);
					if(cstr!=null) {
						Object aafcon = cstr.newInstance(access);
						if(aafcon==null) {
							access.log(Level.INIT,"ERROR! AAF LUR Failed construction.  NOT Configured");
						} else {
							String mechid = getter.get(Config.AAF_MECHID, null,true);
							String pass = getter.get(Config.AAF_MECHPASS, null,false);
							if(mechid!=null && pass!=null) {
								try {
									pass = access.decrypt(pass, false);
									Method basicAuth = aafConClass.getMethod("basicAuth", String.class, String.class);
									// DEBUGGING ONLY!!!!!
									// access.log(Level.INFO, mechid, pass);
									basicAuth.invoke(aafcon, mechid,pass);
								} catch (NoSuchMethodException nsme) {
									// it's ok, don't use
								}
							}
							
							Class<?> aafClass = aafConClass.getClassLoader().loadClass(aafLurClass);
							cstr = aafClass.getConstructor(aafConClass);
							if(cstr!=null) {
								Object aaflur = cstr.newInstance(aafcon);
								if(aaflur==null) {
									access.log(Level.INIT,"ERROR! AAF LUR Failed construction.  NOT Configured");
								} else {
									access.log(Level.INIT,"AAF LUR Configured to ",aafURL);
									lurs.add((Lur)aaflur);
									String debugIDs = getter.get(Config.AAF_DEBUG_IDS, null, true);
									if(debugIDs !=null && aaflur instanceof CachingLur) {
										((CachingLur<?>)aaflur).setDebug(debugIDs);
									}
								}
							}
						}
					}
				} catch(ClassNotFoundException cnfe) {
					access.log(Level.INIT,"AAF LUR Plugin Class is not available (",cnfe.getMessage(),"). If needed, add cadi-aaf-<version>.jar, dme2-<version>.jar and properties");
				} catch (Exception e) {
					access.log(e,"AAF LUR class,",aafLurClass,"could not be constructed with given Constructors.");
				}
			//////////// AAF Lur 1.0 /////////////
			} else { 
				String aafMechID = getter.get(AAF_MECHID, null,true);
				String aafMechPwd = getter.get(AAF_MECHPASS, null,false);
				if(aafMechID==null || aafMechPwd==null) {
					access.log(Level.INIT,"ERROR! AAF requires",AAF_MECHID,"and",AAF_MECHPASS,"to be configured");
				}
				
				String aafDmeTO = getter.get(AAF_CONN_TIMEOUT,AAF_CONN_TIMEOUT_DEF,true);
				int dmeTO = Integer.parseInt(aafDmeTO);
				String aafCleanup = getter.get(AAF_USER_EXPIRES,AAF_USER_EXPIRES_DEF,true); // Default is 10 mins
				long userExp = Long.parseLong(aafCleanup);
				String aafHighCount = getter.get(AAF_HIGH_COUNT,AAF_HIGH_COUNT_DEF,true); // High level before cleaning more per cycle
				int highCount = Integer.parseInt(aafHighCount);
				
				Class<?> aafClass;
				try {
					aafClass = access.classLoader().loadClass(aafLurClass);
					Constructor<?> cstr = aafClass.getConstructor(
						Access.class,
						String.class, // DME URL
						String.class, // mechUser with which to access AAF
						String.class, // mechPasswd with which to access AAF
						int.class,	  // dmeTimeOut
						long.class,	  // userExpiration (when Cached Objects end)
						int.class);   // highCount (rough high number of Objects allowed per cycle)
					if(cstr!=null) {
						aafMechPwd = access.decrypt(aafMechPwd, false);
						Object lur = cstr.newInstance(
						access,
						aafURL,
						aafMechID,
						aafMechPwd, 
						dmeTO,      // dme Time
						userExp,    // Cleanup time
						highCount); // high number of objects desired
						
						if(lur==null) {
							access.log(Level.INIT,"ERROR! AAF LUR Failed construction.  NOT Configured");
						} else {
							access.log(Level.INIT,"AAF LUR Configured to ",aafURL);
							lurs.add((Lur)lur);
						}
					}
				} catch(ClassNotFoundException cnfe) {
					access.log(Level.INIT,"AAF Lur Plugin Class",aafLurClass,"is not available. If needed, add cadi-aaf-<version>.jar and properties");
				} catch (Exception e) {
					access.log(e,"AAF Lur could not be constructed with given Constructors.");
				}
			}
		} 
		

		/////////////////////////////////////////////////////
		// Any Additional passed in Constructor
		/////////////////////////////////////////////////////
		for(Object additional : additionalTafLurs) {
			if(additional instanceof Lur) {
				lurs.add((Lur)additional);
				access.log(Level.INIT, additional);
			}
		}

		/////////////////////////////////////////////////////
		// Return a Lur based on how many there are... 
		/////////////////////////////////////////////////////
		switch(lurs.size()) {
			case 0: 
				access.log(Level.INIT,"WARNING! No CADI LURs configured");
				// Return a NULL Lur that does nothing.
				return new NullLur();
			case 1:
				return lurs.get(0); // Only one, just return it, save processing
			default:
				// Multiple Lurs, use EpiLUR to handle
				Lur[] la = new Lur[lurs.size()];
				lurs.toArray(la);
				return new EpiLur(la);
		}
	}
	
//	public static Symm decryptor(Access access, String keyfile) throws IOException {
//		Symm symm = null;
//		File file = new File(keyfile);
//		try {
//			FileInputStream fis = new FileInputStream(file);
//			try {
//				symm = Symm.baseCrypt().obtain(fis);
//			} finally {
//				fis.close();
//			}
//		} catch (FileNotFoundException e) {
//			String runDir = System.getProperty("user.dir");
//			access.log(Level.INIT,"ERROR! Cannot access specified Keyfile: " + file.getAbsolutePath() +" from " + runDir + ". Check path and permissions.");
//		}
//		return symm;
//	}
//
//	/**
//	 * 
//	 * @param result
//	 * @param specificContent
//	 * @return
//	 */
//	public static String formatConfigMessage(String result, String specificContent) {
//		return specificContent==null?result:String.format(specificContent, result);
//	}

	/**
	 * Convert 
	 * @param access
	 * @param props
	 * @param tags
	 * @return
	 * @throws IOException
	 */
	public static void convertProp(Access access, Properties props, String ... tags) throws IOException {
		for(String tag : tags) {
			String value = props.getProperty(tag);
			if(value!=null) {
				props.setProperty(tag,access.decrypt(value.trim(), true));
			}
		}
	}

	// Set by CSP, or is hostname.
	public static String getDefaultRealm() {
		return defaultRealm;
	}

}
