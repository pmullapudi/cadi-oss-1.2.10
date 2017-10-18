/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletInputStream;

/**
 * BufferedServletInputStream
 * 
 * There are cases in brain-dead middleware (SOAP) where they store routing information in the content.
 * 
 * In HTTP, this requires reading the content from the InputStream which, of course, cannot be re-read.
 * 
 * BufferedInputStream exists to implement the "Mark" protocols for Streaming, which will enable being 
 * re-read.  Unfortunately, J2EE chose to require a "ServletInputStream" as an abstract class, rather than
 * an interface, which requires we create a delegating pattern, rather than the preferred inheriting pattern. 
 * 
 * Unfortunately, the standard "BufferedInputStream" cannot be used, because it simply creates a byte array
 * in the "mark(int)" method of that size.  This is not appropriate for this application, because the Header 
 * can be potentially huge, and if a buffer was allocated to accommodate all possibilities, the cost of memory 
 * allocation would be too large for high performance transactions.
 *
 * 
 * @author jg1555
 *
 */
public class BufferedServletInputStream extends ServletInputStream {
	private static final int NONE = 0;
	private static final int STORE = 1;
	private static final int READ = 2;
	
	private InputStream is;
	private int state = NONE;
	private Capacitor capacitor;

	public BufferedServletInputStream(InputStream is) {
		this.is = is;
		capacitor = null;
	}


	// @Override
	public int read() throws IOException {
		int value=-1;
		if(capacitor==null) {
			value=is.read();
		} else {
			switch(state) {
				case STORE:
					value = is.read();
					if(value>=0) {
						capacitor.put((byte)value);
					}
					break;
				case READ:
					value = capacitor.read();
					if(value<0) {
						capacitor.done();
						capacitor=null; // all done with buffer
						value = is.read();
					}
			}
		} 
		return value;
	}

	// @Override
	public int read(byte[] b) throws IOException {
		return read(b,0,b.length);
	}


	// @Override
	public int read(byte[] b, int off, int len) throws IOException {
		int count = -1;
		if(capacitor==null) {
			count = is.read(b,off,len);
		} else {
			switch(state) {
				case STORE:
					count = is.read(b, off, len);
					if(count>0) {
						capacitor.put(b, off, count);
					}
					break;
				case READ:
					count = capacitor.read(b, off, len);
//					System.out.println("Capacitor read " + count);
					if(count<=0) {
						capacitor.done();
						capacitor=null; // all done with buffer
					}
					if(count<len) {
						int temp = is.read(b, count, len-count);
//						System.out.println("Capacitor done, stream read " + temp);
						if(temp>0) { // watch for -1
							count+=temp;
						} else {
							if(count<=0)count = temp; // must account for Stream coming back -1  
						}
					}
					break;
			}
		}
//		System.out.println("read reports " + count);
		return count;
	}

	// @Override
	public long skip(long n) throws IOException {
		long skipped = capacitor.skip(n);
		if(skipped<n) {
			skipped += is.skip(n-skipped);
		}
		return skipped;
	}


	// @Override
	public int available() throws IOException {
		int count = is.available();
		if(capacitor!=null)count+=capacitor.available();
		return count;		
	}
	
	/**
	 * Return just amount buffered (for debugging purposes, mostly)
	 * @return
	 */
	public int buffered() {
		return capacitor.available();
	}


	// @Override
	public void close() throws IOException {
		if(capacitor!=null) {
			capacitor.done();
			capacitor=null;
		}
		is.close();
	}


	/**
	 * Note: Readlimit is ignored in this implementation, because the need was for unknown buffer size which wouldn't 
	 * require allocating and dumping huge chunks of memory every use, or risk overflow.
	 */
	// @Override
	public synchronized void mark(int readlimit) {
		switch(state) {
			case NONE:
				capacitor = new Capacitor();
				break;
			case READ:
				capacitor.done();
				break;
			// ignore case STORE:
		}
		state = STORE;
	}


	/**
	 * Reset Stream
	 * 
	 * Calling this twice is not supported in typical Stream situations, but it is allowed in this service.  The caveat is that it can only reset
	 * the data read in since Mark has been called.  The data integrity is only valid if you have not continued to read past what is stored.
	 *  
	 */
	// @Override
	public synchronized void reset() throws IOException {
		switch(state) {
			case STORE:
				capacitor.setForRead();
				state = READ;
				break;
			case READ:
				capacitor.reset();
				break;
//				throw new IOException("InputStream is already in READ state");
			case NONE: 
				throw new IOException("InputStream has not been marked");
		}
	}


	// @Override
	public boolean markSupported() {
		return true;
	}
}