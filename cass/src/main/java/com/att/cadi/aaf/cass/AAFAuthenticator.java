/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.aaf.cass;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.cassandra.auth.AuthenticatedUser;
import org.apache.cassandra.auth.IAuthenticator;
import org.apache.cassandra.auth.ISaslAwareAuthenticator;
import org.apache.cassandra.exceptions.AuthenticationException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.RequestExecutionException;

import com.att.cadi.Access.Level;
import com.att.cadi.CredVal.Type;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;

public class AAFAuthenticator extends AAFBase implements ISaslAwareAuthenticator  {

	public boolean requireAuthentication() {
		 return true;
	 }
	  
	  /**
	   * Invoked to authenticate an user
	   */
	  public AuthenticatedUser authenticate(Map<String, String> credentials) throws AuthenticationException {
		    String username = (String)credentials.get("username");
		    if (username == null) {
		      throw new AuthenticationException("'username' is missing");
		    }
		    
		    AAFAuthenticatedUser aau = new AAFAuthenticatedUser(access,username);
		    String fullName=aau.getFullName();
		    access.log(Level.DEBUG, "Authenticating", aau.getName(),"(", fullName,")");
		    
		    String password = (String)credentials.get("password");
		    if (password == null) {
		      throw new AuthenticationException("'password' is missing");
		    } else if(password.startsWith("bsf:")) {
		    	try {
					password = Symm.base64noSplit().depass(password);
				} catch (IOException e) {
					throw new AuthenticationException("AAF bnf: Password cannot be decoded");
				}
	  		} else if(password.startsWith("enc:")) {
				try {
					password = access.decrypt(password, true);
				} catch (IOException e) {
					throw new AuthenticationException("AAF Encrypted Password cannot be decrypted");
				}
		    }
		    
		    if(localLur!=null && localLur.validate(fullName, Type.PASSWORD, password.getBytes())) {
			    aau.setAnonymous(true);
			    aau.setLocal(true);
			    access.log(Level.DEBUG, fullName, "is authenticated locally"); //,password);
	    		return aau;
		    }
		    
		    String aafResponse;
		    try {
		    	aafResponse = aafAuthn.validate(fullName, password);
			    if(aafResponse != null) { // Reason for failing.
			    	access.log(Level.AUDIT, "AAF reports ",fullName,":",aafResponse);
			    	throw new AuthenticationException(aafResponse);
			    }
			    access.log(Level.DEBUG, fullName, "is authenticated"); //,password);
			    // This tells Cassandra to skip checking it's own tables for User Entries.
			    aau.setAnonymous(true);
		    } catch (AuthenticationException ex) {
		    	throw ex;
		    } catch(Exception ex) {
	    		access.log(ex,"Exception validating user");		    		
	    		throw new AuthenticationException("Exception validating user");
		    }
		    
		    return aau; 
	  }
	  
	  public void create(String username, Map<IAuthenticator.Option, Object> options) throws InvalidRequestException, RequestExecutionException {
		  access.log(Level.INFO,"Use AAF CLI to create user");
	  }
	  
	  public void alter(String username, Map<IAuthenticator.Option, Object> options) throws RequestExecutionException {
		  access.log(Level.INFO,"Use AAF CLI to alter user");
	  }
	  
	  public void drop(String username) throws RequestExecutionException {
		  access.log(Level.INFO,"Use AAF CLI to delete user");
	  }
	  
	  public SaslAuthenticator newAuthenticator() {
		  return new ISaslAwareAuthenticator.SaslAuthenticator() {
		    private boolean complete = false;
		    private Map<String, String> credentials;

		    public byte[] evaluateResponse(byte[] clientResponse) throws AuthenticationException {
		      this.credentials = decodeCredentials(clientResponse);
		      this.complete = true;
		      return null;
		    }

		    public boolean isComplete() {
		      return this.complete;
		    }

		    public AuthenticatedUser getAuthenticatedUser() throws AuthenticationException {
		      return AAFAuthenticator.this.authenticate(this.credentials);
		    }

		    private Map<String, String> decodeCredentials(byte[] bytes) throws AuthenticationException {
		    	access.log(Level.DEBUG,"Decoding credentials from client token");
		      byte[] user = null;
		      byte[] pass = null;
		      int end = bytes.length;
		      for (int i = bytes.length - 1; i >= 0; i--)
		      {
		        if (bytes[i] != 0)
		          continue;
		        if (pass == null)
		          pass = Arrays.copyOfRange(bytes, i + 1, end);
		        else if (user == null)
		          user = Arrays.copyOfRange(bytes, i + 1, end);
		        end = i;
		      }

		      if (user == null)
		        throw new AuthenticationException("Authentication ID must not be null");
		      if (pass == null) {
		        throw new AuthenticationException("Password must not be null");
		      }
		      Map<String,String> credentials = new HashMap<String,String>();
		      try {
		    	  credentials.put(IAuthenticator.USERNAME_KEY, new String(user, Config.UTF_8));
		    	  credentials.put(IAuthenticator.PASSWORD_KEY, new String(pass, Config.UTF_8));
				} catch (UnsupportedEncodingException e) {
					throw new AuthenticationException(e.getMessage());
				}
		      return credentials;
		    }
		  };	  
	  }

}

