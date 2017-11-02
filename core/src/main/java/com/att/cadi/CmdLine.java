/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

import com.att.cadi.taf.csp.CSPTaf;



/**
 * A Class to run on command line to determine suitability of environment for certain TAFs.
 * 
 * For instance, CSP supports services only in certain domains, and while dynamic host
 * lookups on the machine work in most cases, sometimes, names and IPs are unexpected (and
 * invalid) for CSP because of multiple NetworkInterfaces, etc
 * 
 *
 */
public class CmdLine {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if(args.length>0) {
			if("digest".equalsIgnoreCase(args[0]) && args.length>2) {
				try {
					Symm symm;
					FileInputStream fis = new FileInputStream(args[2]);
					try {
						symm = Symm.obtain(fis);
					} finally {
						fis.close();
					}
					symm.enpass(args[1], System.out);
					System.out.println();
					System.out.flush();
					return;
					/*  testing code... don't want it exposed
					System.out.println(" ******** Testing *********");
					for(int i=0;i<100000;++i) {
						System.out.println(args[1]);
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						b64.enpass(args[1], baos);
						String pass; 
						System.out.println(pass=new String(baos.toByteArray()));
						ByteArrayOutputStream reconstituted = new ByteArrayOutputStream();
						b64.depass(pass, reconstituted);
						String r = reconstituted.toString();
						System.out.println(r);
						if(!r.equals(args[1])) {
							System.err.println("!!!!! STOP - ERROR !!!!!");
							return;
						}
						System.out.println();
					}
					System.out.flush();
					*/
					 
				} catch (IOException e) {
					System.err.println("Cannot digest password");
					System.err.println("   \""+ e.getMessage() + '"');
				}
// DO NOT LEAVE THIS METHOD Compiled IN CODE... Do not want looking at passwords on disk too easy
// Oh, well, Deployment services need this behavior.  I will put this code in, but leave it undocumented. 
// One still needs access to the keyfile to read.
			} else if("regurgitate".equalsIgnoreCase(args[0]) && args.length>2) {
				try {
					Symm symm;
					FileInputStream fis = new FileInputStream(args[2]);
					try {
						symm = Symm.obtain(fis);
					} finally {
						fis.close();
					}
					symm.depass(args[1], System.out);
					System.out.println();
					System.out.flush();
					return;
				} catch (IOException e) {
					System.err.println("Cannot regurgitate password");
					System.err.println("   \""+ e.getMessage() + '"');
				}
			} else if("encode64".equalsIgnoreCase(args[0]) && args.length>1) {
				try {
					Symm.base64.encode(args[1], System.out);
					System.out.println();
					System.out.flush();
					return;
				} catch (IOException e) {
					System.err.println("Cannot encode Base64 with " + args[1]);
					System.err.println("   \""+ e.getMessage() + '"');
				}
			} else if("decode64".equalsIgnoreCase(args[0]) && args.length>1) {
				try {
					Symm.base64.decode(args[1], System.out);
					System.out.println();
					System.out.flush();
					return;
				} catch (IOException e) {
					System.err.println("Cannot decode Base64 text from " + args[1]);
					System.err.println("   \""+ e.getMessage() + '"');
				}
			} else if("encode64url".equalsIgnoreCase(args[0]) && args.length>1) {
				try {
					Symm.base64url.encode(args[1], System.out);
					System.out.println();
					System.out.flush();
					return;
				} catch (IOException e) {
					System.err.println("Cannot encode Base64url with " + args[1]);
					System.err.println("   \""+ e.getMessage() + '"');
				}
			} else if("decode64url".equalsIgnoreCase(args[0]) && args.length>1) {
				try {
					Symm.base64url.decode(args[1], System.out);
					System.out.println();
					System.out.flush();
					return;
				} catch (IOException e) {
					System.err.println("Cannot decode Base64url text from " + args[1]);
					System.err.println("   \""+ e.getMessage() + '"');
				}
			} else if("md5".equalsIgnoreCase(args[0]) && args.length>1) {
				try {
					System.out.println(Hash.encryptMD5asStringHex(args[1]));
					System.out.flush();
				} catch (NoSuchAlgorithmException e) {
					System.err.println("Cannot hash MD5 from " + args[1]);
					System.err.println("   \""+ e.getMessage() + '"');
				}
				return;
			} else if("sha256".equalsIgnoreCase(args[0]) && args.length>1) {
				try {
					System.out.println(Hash.hashSHA256asStringHex(args[1]));
					System.out.flush();
				} catch (NoSuchAlgorithmException e) {
					System.err.println("Cannot hash SHA256 text from " + args[1]);
					System.err.println("   \""+ e.getMessage() + '"');
				}
				return;
			} else if("keygen".equalsIgnoreCase(args[0])) {
				try {
					if(args.length>1) {
						FileOutputStream fos = new FileOutputStream(args[1]);
						try {
							fos.write(Symm.baseCrypt().keygen());
							fos.flush();
						} finally {
							fos.close();
						}
					} else {
						// create a Symmetric Key out of same characters found in base64
						System.out.write(Symm.baseCrypt().keygen());
						System.out.flush();
					}
					return;
				} catch (IOException e) {
					System.err.println("Cannot create a key " + args[0]);
					System.err.println("   \""+ e.getMessage() + '"');
				}
			
			} else if("passgen".equalsIgnoreCase(args[0])) {
				int numDigits;
				if(args.length < 1) {
					numDigits = 24;
				} else {
					numDigits = Integer.parseInt(args[1]); 
					if(numDigits<8)numDigits = 8;
				}
				String pass;
				boolean noLower,noUpper,noDigits,noSpecial,repeats;
				do {
					pass = Symm.randomGen(numDigits);
					noLower=noUpper=noDigits=noSpecial=true;
					repeats=false;
					int c=-1,last;
					for(int i=0;i<numDigits;++i) {
						last = c;
						c = pass.charAt(i);
						if(c==last) {
							repeats=true;
							break;
						}
						
						if(noLower) {
							noLower=!(c>=0x61 && c<=0x7A);
							continue;
						} 
						if(noUpper) {
							noUpper=!(c>=0x41 && c<=0x5A);
							continue;
						} 
						if(noDigits) {
							noDigits=!(c>=0x30 && c<=0x39);
							continue;
						} 
						if(noSpecial) {
							noSpecial = "+!@#$%^&*(){}[]?:;,.".indexOf(c)<0;
							continue;
						} 
						
						break;
					}
				} while(noLower || noUpper || noDigits || noSpecial || repeats);
				System.out.println(pass.substring(0,numDigits));
			} else if("urlgen".equalsIgnoreCase(args[0])) {
				int numDigits;
				if(args.length < 1) {
					numDigits = 24;
				} else {
					numDigits = Integer.parseInt(args[1]); 
				}
				System.out.println(Symm.randomGen(Symm.base64url.codeset, numDigits).substring(0,numDigits));
			
			} else if("csptest".equalsIgnoreCase(args[0])) {
				try {
					System.out.println("CSP Compatibility test");
					
					String hostName = InetAddress.getLocalHost().getCanonicalHostName();
					
					System.out.println("  Your automatic hostname is reported as \"" + hostName + "\"\n");
					System.out.println(CSPTaf.domainSupportedCSP(hostName, "  "));
					System.out.flush();
					return;
				} catch (UnknownHostException e) {
					e.printStackTrace(System.err);
				}
			}
		} else {
			System.out.println("Usage: java -jar <this jar> ...");
			System.out.println("  keygen [<keyfile>]                     (Generates Key on file, or Std Out)");
			System.out.println("  passgen <digits>                       (Generate Password of given size)");
			System.out.println("  urlgen <digits>                        (Generate URL field of given size)");
			System.out.println("  digest <your password> <keyfile>       (Encrypts to Key with \"keyfile\")");
			System.out.println("  csptest                                (Tests for CSP compatibility)");
			System.out.println("  encode64 <your text>                   (Encodes to Base64)");
			System.out.println("  decode64 <base64 encoded text>         (Decodes from Base64)");
			System.out.println("  encode64url <your text>                (Encodes to Base64 URL charset)");
			System.out.println("  decode64url <base64url encoded text>   (Decodes from Base64 URL charset)");
			System.out.println("  sha256 <text>                          (Digest String into SHA256 Hash)");
			System.out.println("  md5 <text>                             (Digest String into MD5 Hash)");
		}
		System.exit(1);
	}

}
