/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.CadiException;
import com.att.cadi.CadiWrap;
import com.att.cadi.CredVal;
import com.att.cadi.Lur;
import com.att.cadi.Taf;
import com.att.cadi.config.Config;
import com.att.cadi.config.Get;
import com.att.cadi.config.MultiGet;
import com.att.cadi.lur.EpiLur;
import com.att.cadi.lur.NullLur;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.NullTaf;
import com.att.cadi.taf.TafResp;

/**
 * CadiFilter
 * 
 * This class implements Servlet Filter, and ties together CADI implementations
 * 
 * This class can be used in a standard J2EE Servlet manner.  Optimal usage is for POJO operations, where
 * one can enforce this Filter being first and primary.  Depending on the Container, it 
 * may be more effective, in some cases, to utilize features that allow earlier determination of 
 * AUTHN (Authorization).  An example would be "Tomcat Valve".  These implementations, however, should
 * be modeled after the "init" and "doFilter" functions, and be kept up to date as this class changes.
 * 
 * 
 * @author jg1555
 *
 */
public class CadiFilter extends CadiAccess implements Filter {
	private static HttpTaf epiTaf = null;
	private static Lur lur = null;
	private static CredVal up = null;
	private Object additionalTafLurs[];
	private String[] pathExceptions;
	private ArrayList<Pair> mapPairs;
	
	private static final Object[] noAdditional = new Object[0]; // CadiFilter can be created each call in some systems
	

	public Lur getLur() {
		return lur;
	}
	
	/**
	 * Construct a viable Filter
	 * 
	 * Due to the vagaries of many containers, there is a tendency to create Objects and call "Init" on 
	 * them at a later time.  Therefore, this object creates with an object that denies all access
	 * until appropriate Init happens, just in case the container lets something slip by in the meantime.
	 * 
	 */
	public CadiFilter() {
		super(null);
		// Lock down any behavior while uninitialized
		// 4/15/2014 - jg - found an issue where if another CadiFilter is created after first one, then the NullTaf overwrites
		// Config.  Therefore, only initialize if the object is null.
		// This construct is to avoid synchronizing if possible, as some Containers may create new filters at unknown times.
		if(epiTaf==null) {
			// Don't start changing unless have lock
			synchronized(NullTaf.singleton()) {
				if(epiTaf==null) epiTaf = NullTaf.singleton();
				if(lur==null) lur = new NullLur();
			}
		}
		additionalTafLurs = noAdditional; // CadiFilter may be called for each transaction in some Containers
	}
	
	public CadiFilter(Access access, Object ... moreTafLurs) {
		super(null);
		// Lock down any behavior while uninitialized
		// 4/15/2014 - jg - found an issue where if another CadiFilter is created after first one, then the NullTaf overwrites
		// Config.  Therefore, only initialize if the object is null.
		// This construct is to avoid synchonizing if possible, as some Containers may create new filters at unknown times.
		if(epiTaf==null) {
			// Don't start changing unless have lock
			synchronized(NullTaf.singleton()) {
				if(epiTaf==null) epiTaf = NullTaf.singleton();
				if(lur==null) lur = new NullLur();
			}
		}
		additionalTafLurs = moreTafLurs;
		getter = new AccessGetter(access);
	}


	/**
	 * Init
	 * 
	 * Standard Filter "init" call with FilterConfig to obtain properties.  POJOs can construct a
	 * FilterConfig with the mechanism of their choice, and standard J2EE Servlet engines utilize this
	 * mechanism already.
	 */
	//TODO Always validate changes against Tomcat AbsCadiValve and Jaspi CadiSAM Init functions
	public void init(FilterConfig filterConfig) throws ServletException {
		// need the Context for Logging, instantiating ClassLoader, etc
		context = filterConfig.getServletContext();
		
		// Set Protected getter with base Access, for internal class instantiations
		init(new FCGet(this, context, filterConfig));
	}
	
	public void init(Access access) throws ServletException {	
		init(new AccessGetter(access));
	}
	

    public void init(Get getter) throws ServletException {
        super.getter = new MultiGet(getter,new AccessGetter(this));

        try {
            Config.configPropFiles(super.getter, this);  
        }
        catch (Exception e )
        {
            throw new ServletException(e);
        }
            

        // Choose Log level (make sure you have capacity for heavily used systems)
        willWrite = Level.valueOf(super.getter.get(Config.CADI_LOGLEVEL,Level.INFO.name(),true));

		synchronized(NullTaf.singleton()) {
			if(epiTaf == NullTaf.singleton()) {
				try {
					
					lur = Config.configLur(super.getter, this, additionalTafLurs); 
					if(lur instanceof EpiLur) {
						up = ((EpiLur)lur).getUserPassImpl();
					} else if(lur instanceof CredVal) {
						up = (CredVal)lur;
					} else {
						up = null;
					}
					epiTaf = Config.configHttpTaf(this, super.getter, up, lur, additionalTafLurs);
				} catch (CadiException e1) {
					throw new ServletException(e1);
				}
			}

			/*
			 * Setup Authn Path Exceptions
			 */
			String str = super.getter.get(Config.CADI_NOAUTHN, null, true);
			if(str!=null) {
				pathExceptions = str.split("\\s*:\\s*");
			}
	
			/* 
			 * SETUP Permission Converters... those that can take Strings from a Vendor Product, and convert to appropriate AAF Permissions
			 */
			if((str = super.getter.get(Config.AAF_PERM_MAP, null, true))!=null) {
				String mstr = super.getter.get(Config.AAF_PERM_MAP, null, true);
				if(mstr!=null) {
					String map[] = mstr.split("\\s*:\\s*");
					if(map.length>0) {
						MapPermConverter mpc=null;
						int idx;
						mapPairs = new ArrayList<Pair>();
						for(String entry : map) {
							if((idx=entry.indexOf('='))<0) { // it's a Path, so create a new converter
								log(Level.INIT,"Loading Perm Conversions for:",entry);
								mapPairs.add(new Pair(entry,mpc=new MapPermConverter()));
							} else {
								if(mpc!=null) {
									mpc.map().put(entry.substring(0,idx),entry.substring(idx+1));
								} else {
									log(Level.ERROR,"cadi_perm_map is malformed; ",entry, "is skipped");
								}
							}
						}
					}
				}
			}
		}

		// Remove Getter
        getter = Get.NULL;

	}

	/**
	 * Containers call "destroy" when time to cleanup 
	 */
	public void destroy() {
		log(Level.INFO,"CadiFilter destroyed.");
		if(lur!=null)lur.destroy();
	}

	/**
	 * doFilter
	 * 
	 * This is the standard J2EE invocation.  Analyze the request, modify response as necessary, and
	 * only call the next item in the filterChain if request is suitably Authenticated.
	 */
	//TODO Always validate changes against Tomcat AbsCadiValve and Jaspi CadiSAM functions
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		try {
			HttpServletRequest hreq = (HttpServletRequest)request;
			if(noAuthn(hreq)) {
				chain.doFilter(request, response);
			} else {
				HttpServletResponse hresp = (HttpServletResponse)response;
				TafResp tresp = epiTaf.validate(Taf.LifeForm.LFN, hreq, hresp);
				switch(tresp.isAuthenticated()) {
					case IS_AUTHENTICATED:
						log(Level.AUDIT,"Valid Credential:", tresp.desc(), FROM, request.getRemoteAddr(), ':', request.getRemotePort());
						chain.doFilter(new CadiWrap(hreq, tresp, lur,getConverter(hreq)),response);
						break;
					case TRY_AUTHENTICATING:
						boolean sec = !tresp.isValid() && tresp.desc().startsWith("User/Pass");
						if(sec) {
							log(Level.AUDIT, tresp.desc());
						}
	
						switch (tresp.authenticate()) {
							case IS_AUTHENTICATED:
								if(sec)
									log(Level.INFO,"Authenticated: ", tresp.desc());
								else 
									log(Level.INFO,"Authenticated: ", tresp.desc(), FROM, request.getRemoteAddr(), ':', request.getRemotePort());
								chain.doFilter(new CadiWrap(hreq, tresp, lur,getConverter(hreq)),response);
								break;
							case HTTP_REDIRECT_INVOKED:
								log(Level.INFO,"Authenticating via redirection: ", tresp.desc());
								break;
							case NO_FURTHER_PROCESSING:
								log(Level.AUDIT,"Authentication Failure: ", tresp.desc(), FROM, request.getRemoteAddr(), ':', request.getRemotePort());
								hresp.sendError(403, tresp.desc()); // Forbidden
								break;
							default:
								log(Level.AUDIT,"No TAF will authorize for request from ", request.getRemoteAddr(), ':', request.getRemotePort());
								hresp.sendError(403, tresp.desc()); // Forbidden
						}
						break;
					case NO_FURTHER_PROCESSING:
						log(Level.AUDIT,"Authentication Failure: ", tresp.desc(), FROM, request.getRemoteAddr(), ':', request.getRemotePort());
						hresp.sendError(403, "Access Denied"); // FORBIDDEN
						break;
					default:
						log(Level.AUDIT,"No TAF will authorize for request from ", request.getRemoteAddr(), ':', request.getRemotePort());
						hresp.sendError(403, "Access Denied"); // FORBIDDEN
				}
			}
		} catch (ClassCastException e) {
			throw new ServletException("CadiFilter expects Servlet to be an HTTP Servlet",e);
		}
	}

	/** 
	 * If PathExceptions exist, report if these should not have Authn applied.
	 * @param hreq
	 * @return
	 */
	private boolean noAuthn(HttpServletRequest hreq) {
		if(pathExceptions!=null) {
			String pi = hreq.getPathInfo();
			if(pi==null) return false; // JBoss sometimes leaves null
			for(String pe : pathExceptions) {
				if(pi.startsWith(pe))return true;
			}
		}
		return false;
	}
	
	/**
	 * Get Converter by Path
	 */
	private PermConverter getConverter(HttpServletRequest hreq) {
		if(mapPairs!=null) {
			String pi = hreq.getPathInfo();
			for(Pair p: mapPairs) {
				if(pi.startsWith(p.name))return p.pc;
			}
		}
		return NullPermConverter.singleton();
	}
	
	/**
	 * store PermConverters by Path prefix
	 * @author jg1555
	 *
	 */
	private class Pair {
		public Pair(String key, PermConverter pc) {
			name = key;
			this.pc = pc;
		}
		public String name;
		public PermConverter pc;
	}

}

