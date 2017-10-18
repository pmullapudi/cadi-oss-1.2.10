/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.io.IOException;
import java.io.InputStream;

/**
 * Various Environments require different logging mechanisms, or at least allow
 * for different ones. We need the Framework to be able to hook into any particular instance of logging
 * mechanism, whether it be a Logging Object within a Servlet Context, or a direct library like log4j.
 * This interface, therefore, allows maximum pluggability in a variety of different app styles.  
 *  
 * @author jg1555
 *
 */
public interface Access {

	// levels to use
	enum Level {DEBUG, INFO, AUDIT, INIT, WARN, ERROR};
	
	/**
	 * Write a variable list of Object's text via the toString() method with appropriate space, etc.
	 * @param elements
	 */
	public void log(Level level, Object ... elements);

	/**
	 * Write the contents of an exception, followed by a variable list of Object's text via the 
	 * toString() method with appropriate space, etc.
	 * 
	 * The Loglevel is always "ERROR"
	 * 
	 * @param elements
	 */
	public void log(Exception e, Object ... elements);
	
	/**
	 * Set the Level to compare logging too
	 */
	public void setLogLevel(Level level);
		
	/**
	 * It is important in some cases to create a class from within the same Classloader that created
	 * Security Objects.  Specifically, it's pretty typical for Web Containers to separate classloaders
	 * so as to allow Apps with different dependencies. 
	 * @return
	 */
	public ClassLoader classLoader();

	public String getProperty(String string, String def);
	
	public void load(InputStream is) throws IOException;

	/**
	 * if "anytext" is true, then decryption will always be attempted.  Otherwise, only if starts with 
	 * Symm.ENC
	 * @param encrypted
	 * @param anytext
	 * @return
	 * @throws IOException
	 */
	public String decrypt(String encrypted, boolean anytext) throws IOException;
}
