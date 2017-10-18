/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.principal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.Principal;
import java.util.Date;

import com.att.cadi.BasicCred;
import com.att.cadi.GetCred;
import com.att.cadi.Symm;

public class BasicPrincipal implements GetCred, Principal {
	private static byte[] basic = "Basic ".getBytes();

	private String content;
	private String name = null;
	private String shortName = null;
	private byte[] cred = null;  
	
	private long created;

	public BasicPrincipal(String content,String domain) throws IOException {
		this.content = content; // reuse
		created = System.currentTimeMillis();
		ByteArrayInputStream bis = new ByteArrayInputStream(content.getBytes());
		// Read past "Basic ", ensuring it starts with it.
		for(int i=0;i<basic.length;++i) {
			if(bis.read()!=basic[i]) {
				name=content;
				cred = null;
				return;
			}
		}
		BasicOS bos = new BasicOS(content.length());
		Symm.base64.decode(bis,bos); // note: writes directly to name until ':'
		if(name==null) throw new IOException("Invalid Coding");
		else cred = bos.toCred();
		int at;
		if((at=name.indexOf('@'))>0) {
			domain=name.substring(at+1);
			shortName=name.substring(0, at);
		} else {
			shortName = name;
			name = name + '@' + domain;
		}
	}
	
	public BasicPrincipal(BasicCred bc, String domain) {
		content = null;
		name = bc.getUser();
		cred = bc.getCred();
	}

	private class BasicOS extends OutputStream {
		private boolean first = true;
		private ByteArrayOutputStream baos;
		
		public BasicOS(int size) {
			baos = new ByteArrayOutputStream(size);
		}

		@Override
		public void write(int b) throws IOException {
			if(b==':' && first) {
				first = false;
				name = new String(baos.toByteArray());
				baos.reset(); // 
			} else {
				baos.write(b);
			}
		}
		
		private byte[] toCred() {
			return baos.toByteArray();
		}
	}
	
	public String getAsHeader() throws IOException {
		if(content==null) {
			content = "Basic " + Symm.base64.encode(name + ':' + cred);  
		}
		return content;
	}
	
	public String getName() {
		return name;
	}
	
	public String getShortName() {
		return shortName;
	}
	
	public byte[] getCred() {
		return cred;
	}
	
	public long created() {
		return created;
	}

	public String toString() {
		return "Basic Authorization for " + name + " evaluated on " + new Date(created).toString();
	}
}
