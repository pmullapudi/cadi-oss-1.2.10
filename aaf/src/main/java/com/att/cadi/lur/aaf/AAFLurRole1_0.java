/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf;

import java.io.StringReader;
import java.io.Writer;
import java.security.Principal;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aaf.xsd.Roles;

import com.att.aft.dme2.api.DME2Client;
import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.CadiException;
import com.att.cadi.Permission;
import com.att.cadi.User;
import com.att.rosetta.OutJax;

/**
 * A Lur that checks AAF Roles (not Permissions.  For Permissions, see AAFLur)
 * 
 *
 */
public class AAFLurRole1_0 extends AbsAAFLur<AAFRole> {
	private Pattern att_mechID;
	/**
	 *  Need to be able to transmutate a Principal into either ATTUID or MechID, which are the only ones accepted at this
	 *  point by AAF.  There is no "domain", aka, no "@att.com" in "ab1234@att.com".  
	 *  
	 *  The only thing that matters here for AAF is that we don't waste calls with IDs that obviously aren't valid.
	 *  Thus, we validate that the ID portion follows the rules before we waste time accessing AAF remotely
	 */

	public AAFLurRole1_0(Access access, String dmeServiceName, String mechUser, String mechPasswd, int dmeTimeOut, long userExpiration, int highCount) throws CadiException {
		super(access, dmeServiceName, mechUser, mechPasswd, dmeTimeOut, userExpiration, highCount,2);
		att_mechID = Pattern.compile("[a-zA-Z][a-zA-Z0-9][0-9]{3}[a-zA-Z0-9]");
	}

	@Override
	protected boolean isCorrectPermType(Permission pond) {
		return pond instanceof AAFRole;
	}

	@Override
	protected User<AAFRole> loadUser(Principal bait) throws Exception {
		// Note: The rules for AAF is that it only stores permissions for ATTUID and MechIDs, which don't 
		// have domains.  We are going to make the Transitive Class (see this.transmutative) to convert
		Principal principal = transmutate.mutate(bait);
		if(principal==null)return null; // if not a valid Transmutated credential, don't bother calling...
		String name = principal.getName();
		long start = System.nanoTime();
		DME2Client client = newClient();
		try {
			client.setPayload("/authz/users/" + name + "/roles");
			
			String responseText = client.sendAndWait(dmeTimeout);
			JaxSetDocRoles jsd = new JaxSetDocRoles();
			new OutJax(jsd).extract(new StringReader(responseText),(Writer)null,inJSON);
			
			User<AAFRole> user = getUser(bait);
			if(user==null) user = new User<AAFRole>(bait,userExpiration); // no password
			addUser(user);
			Roles roles = jsd.getRoles();
			if (roles != null) {
				Map<String, Permission> newMap = user.newMap();
				for(aaf.xsd.Role role : roles.getRoles()) {
					user.add(newMap,new AAFRole(role));
				}
				user.setMap(newMap);
			}
			
			return user;
		} finally {
			float time = (System.nanoTime()-start)/1000000f;
			access.log(Level.AUDIT, "Remote AAF Service Lookup",name,"in",time,"ms");
		}
	}
	/* 
	 * Note: AAF 1.0 allows for IDs that are AT&T only... (no Domain).
	 * @see com.att.cadi.lur.aaf.AbsAAFLur#supports(java.lang.String)
	 */
	@Override
	public boolean supports(String userName) {
		if(userName==null)return false;
		Matcher m = att_mechID.matcher(userName);
		return m==null?false:m.matches();
	}


}
