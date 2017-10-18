/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.csp;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.Principal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.att.cadi.CachedPrincipal;
import com.att.cadi.config.Config;

/**
 * Note: we don't put this Principal in the main Principal folder, because we want to 
 * hide the extract function
 * 
 * @author jg1555
 *
 */
public class CSPPrincipal implements CachedPrincipal {
	/* Authorization Strength, per CSP Doc "ESAuth_UGuide.doc, 1/25/2012
	10 = non-aged password (aka normal strength)
	20 = ASPR compliant password (aka high-strength)
	25 = Forced ASPR password
	40 = SecureID Token 
	60 = Digital Certificate
	*/
	public final static int NON_AGED_PASSWORD = 10;
	public final static int ASPR_COMPLIANT_PASSWORD = 20;
	public final static int FORCED_ASPR_PASSWORD = 25;
	public final static int SECUREID_TOKEN = 40;
	public final static int DIGITAL_CERT = 60;
	private static final String AT_CSP_ATT_COM = "@csp.att.com";
	
	// AttESSec info
	private final String attessec; // original Cookie 
	private final String[] essec;
	private final String atteshr;

	private String [] eshr; // only loaded on demand
	
	// These take processing, and typically unused, so we'll create on first use only 
	private int auth_strength;
	private String[] privileges;
	private Calendar timestamp; // last validation + 13 hours
	private String name;

	private static final String PIPES = "\\|";
	private static final String EMPTY = "";

	private CSPPrincipal(String attessec, String decryptedInfo, String atteshr) {
		this.attessec = attessec;
		essec = decryptedInfo.split(PIPES);
		eshr = null;
		// We won't break this out unless needed.
		this.atteshr = atteshr;
		auth_strength=-1;
		privileges = null;
		timestamp = null;
	}
	
//	@Override
	public String getName() {
		if (name == null) name = essec[5] + AT_CSP_ATT_COM;
		return name;
	}
	
	public String getShortName() {
		return essec[5];
	}

	/**
	 * Revalidating CSP Principal only requires checking the Expiration Data (which is converted to Timestamp by method) 
	 */
	public Resp revalidate() {
		return getTimestamp().getTimeInMillis()>System.currentTimeMillis() // if timestamp is "after the NOW"
			?Resp.UNVALIDATED
			:Resp.REVALIDATED;
	}

	public long expires() {
		return getTimestamp().getTimeInMillis();
	}

	//package on purpose
//	static CSPPrincipal extract(String attessec, String cspEnv, String atteshr) {
		Object esGateKeeper;
//		String derived = esGateKeeper.esGateKeeper(attessec, "CSP", cspEnv);
//		return "".equals(derived)?null:new CSPPrincipal(attessec, derived,atteshr);
//	}

	public String getHrid() {
		return essec[0];
	}

	public String getMd5() {
		return essec[1];
	}

	public String getEmpl_type() {
		return essec[2];
	}

	public int getAuth_strength() {
		if(auth_strength<0) { 
			try {
				auth_strength=Integer.parseInt(essec[3]);
			} catch (Exception e) {
				// An exception here would be corrupted content... just set zero
				auth_strength = 0;
			}
		}
		return auth_strength;
	}

	public String[] getPrivileges() {
		if(privileges==null) {
			privileges = essec[4].split(",");
		}
		return privileges;
	}

	public String getEnv() {
		return essec[6];
	}

	public String getExpires() {
		return essec[7];
	}

	public Calendar getTimestamp() {
		if(timestamp==null) {
			String date = essec[7];
			timestamp = new GregorianCalendar(
				Integer.parseInt(date.substring(0,4)), // year
				Integer.parseInt(date.substring(4,6))-1, // Month
				Integer.parseInt(date.substring(6,8)), // day
				Integer.parseInt(date.substring(8,10)), // hour
				Integer.parseInt(date.substring(10)) // min
				);
		}
		return timestamp;
	}
	
	/**
	 *  Be able to check if HR Info was included... do not call any "HR" functions before checking.
	 * @return
	 */
	public boolean hasHRInfo() {
		return atteshr!=null;
	}
	
	public String getHRFirstName() {
		return loadHR()?eshr[0]:EMPTY;
	}
	
	public String getHRLastName() {
		return loadHR()?eshr[1]:EMPTY;
	}
	
	public String getHREmail() {
		return loadHR()?eshr[2]:EMPTY;
	}
	
	public String getHRWorkPhone() {
		return loadHR()?eshr[3]:EMPTY;
	}
	
	public String getHRMiddleName() {
		return loadHR()?eshr[4]:EMPTY;
	}
	
	public String getHRManagerATTUID() {
		return loadHR()?eshr[5]:EMPTY;
	}
	
	public String getHRNameSuffix() {
		return loadHR()?eshr[6]:EMPTY;
	}
	
	public String[] getHRLegacyUIDs() {
		return loadHR()?eshr[7].split(","):new String[0];
	}
	
	public String getHRPatternA() {
		return loadHR()?eshr[8]:EMPTY;
	}

	public String getHRNickName() {
		return loadHR()?eshr[9]:EMPTY;
	}
	

	public String getHRCostCenter() {
		return loadHR()?eshr[10]:EMPTY;
	}
	
	public String getHRSalaryGrade() {
		return loadHR()?eshr[11]:EMPTY;
	}

	public String getAsCspAuthHeader() {
		return "CSP " + attessec;
	}
	
	// Lazy Instantiation of HR data...
	private synchronized boolean loadHR() {
		if(eshr==null) {
			try {
				eshr = URLDecoder.decode(atteshr, Config.UTF_8).split(PIPES);
			} catch (UnsupportedEncodingException e) {
				// TODO Log??
				eshr= new String[0];
			}
		}
		return eshr.length>=11; // eleven |s are guaranteed by CSP
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("CSP Principal for ");
		sb.append(getEnv());
		sb.append(", expires ");
		sb.append(SimpleDateFormat.getInstance().format(getTimestamp().getTime()));
		sb.append(" EDT\n MD5        : ");
		sb.append(getMd5());
		sb.append("\n Auth Strength  : ");
		sb.append(getAuth_strength());

		sb.append("\n  Name          : ");
		sb.append(getName());
		sb.append("\n  EmployeeType  : ");
		sb.append(getEmpl_type());
		sb.append("\n  Privileges    : ");
		for(String str : getPrivileges()) {
			sb.append("\n    ");
			sb.append(str);
		}
		
		// HR Info
		if(hasHRInfo()) {
			sb.append("\n HR derived Info\n  Name          : ");
			sb.append(getHRFirstName());
			sb.append(' ');
			String str = getHRMiddleName();
			if(str.length()>0) {
				sb.append(str);
				sb.append(' ');
			}
			sb.append(getHRLastName());
			str = getHRNameSuffix();
			if(str.length()>0) {
				sb.append(' ');
				sb.append(str);
			}
			str = getHRNickName();
			if(str.length()>0) {
				sb.append(" (a.k.a. ");
				sb.append(str);
				sb.append(')');
			}
			
			
			sb.append("\n  Email         : ");
			sb.append(getHREmail());
			sb.append("\n  Work Phone    : ");
			sb.append(getHRWorkPhone());
			sb.append("\n  Manager ATTUID: ");
			sb.append(getHRManagerATTUID());
			sb.append("\n  Legacy UIDs   : ");
			for(String uid : getHRLegacyUIDs()) {
				sb.append("\n    ");
				sb.append(uid);
			}
			sb.append("\n  Pattern A     : ");
			sb.append(getHRPatternA());
			sb.append("\n  Cost Center   : ");
			sb.append(getHRCostCenter());
			sb.append("\n  Salary Grade  : ");
			sb.append(getHRSalaryGrade());
		}
		return sb.toString();
	}

	public String attessec() {
		return attessec;
	}

	public static Principal extract(String attsesec, String cspEnv, String atteshr2) {
		// TODO Auto-generated method stub
		return null;
	}

}
