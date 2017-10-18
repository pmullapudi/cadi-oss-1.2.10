/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.lur.aaf.test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;

import com.att.cadi.Access;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;

public class TestAccess implements Access {
	private Symm symm;
	private PrintStream out;

	public TestAccess(PrintStream out) {
		this.out = out;
		InputStream is = ClassLoader.getSystemResourceAsStream("cadi.properties");
		try {
			System.getProperties().load(is);
		} catch (IOException e) {
			e.printStackTrace(out);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace(out);
			}
		}
		
		String propfiles = System.getProperty(Config.CADI_KEYFILE);
		if(propfiles==null) {
			System.err.println("No " + Config.CADI_KEYFILE + " in Classpath");
		} else {
			try {
				is = new FileInputStream(propfiles);
				try {
					symm = Symm.obtain(is);
				} finally {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace(out);
			}
		}
		


	}
	
	public void log(Level level, Object... elements) {
		boolean first = true;
		for(int i=0;i<elements.length;++i) {
			if(first)first = false;
			else out.print(' ');
			out.print(elements[i].toString());
		}
		out.println();
	}

	public void log(Exception e, Object... elements) {
		e.printStackTrace(out);
		log(Level.ERROR,elements);
	}

	public void setLogLevel(Level level) {
		
	}

	public ClassLoader classLoader() {
		return ClassLoader.getSystemClassLoader();
	}

	public String getProperty(String string, String def) {
		String rv = System.getProperty(string);
		return rv==null?def:rv;
	}

	public void load(InputStream is) throws IOException {
		
	}

	public String decrypt(String encrypted, boolean anytext) throws IOException {
		return (encrypted!=null && (anytext==true || encrypted.startsWith(Symm.ENC)))
			? symm.depass(encrypted)
			: encrypted;
	}

}
