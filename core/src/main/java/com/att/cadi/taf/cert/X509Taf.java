/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf.cert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.att.cadi.Access;
import com.att.cadi.Access.Level;
import com.att.cadi.CachedPrincipal;
import com.att.cadi.CachedPrincipal.Resp;
import com.att.cadi.CadiException;
import com.att.cadi.Lur;
import com.att.cadi.Symm;
import com.att.cadi.Taf.LifeForm;
import com.att.cadi.config.Config;
import com.att.cadi.lur.LocalPermission;
import com.att.cadi.principal.TGuardPrincipal;
import com.att.cadi.taf.HttpTaf;
import com.att.cadi.taf.TafResp;
import com.att.cadi.taf.TafResp.RESP;

public class X509Taf implements HttpTaf {
	
	public static final CertificateFactory certFactory;
	public static final MessageDigest messageDigest;
	private Access access;
	private CertIdentity[] certIdents;
	private Lur lur;

	static {
		try {
			certFactory = CertificateFactory.getInstance("X.509");
			messageDigest = MessageDigest.getInstance("SHA-256"); // use this to clone
		} catch (Exception e) {
			throw new RuntimeException("X.509 and SHA-256 are required for X509Taf",e);
		}
	}
	
	public X509Taf(Access access, Lur lur, CertIdentity ... cis) throws CertificateException, NoSuchAlgorithmException, CadiException {
		this.access = access;
		this.lur = lur;
		try {
			Class<?> dci = access.classLoader().loadClass("com.att.authz.cadi.DirectCertIdentity");
			CertIdentity temp[] = new CertIdentity[cis.length+1];
			System.arraycopy(cis, 0, temp, 1, cis.length);
			temp[0] = (CertIdentity) dci.newInstance();
			certIdents=temp;
		} catch (Exception e) {
			certIdents = cis;
		}
	}

	public static final X509Certificate getCert(byte[] certBytes) throws CertificateException {
		ByteArrayInputStream bais = new ByteArrayInputStream(certBytes);
		return (X509Certificate)certFactory.generateCertificate(bais);
	}

	public static final byte[] getFingerPrint(byte[] ba) {
		MessageDigest md;
		try {
			md = (MessageDigest)messageDigest.clone();
		} catch (CloneNotSupportedException e) {
			// should never get here
			return new byte[0];
		}
		md.update(ba);
		return md.digest();
	}

	public TafResp validate(LifeForm reading, HttpServletRequest req, HttpServletResponse resp) {
		// Check for Mutual SSL
		byte[] array = null;
		byte[] certBytes = null;
		try {
			X509Certificate[] certarr = (X509Certificate[])req.getAttribute("javax.servlet.request.X509Certificate");
			X509Certificate cert;
			String responseText;
			String authHeader = req.getHeader("Authorization");

			if(certarr!=null) {  // If cert !=null, Cert is Tested by Mutual Protocol.
				if(authHeader!=null) { // This is only intended to be a Secure Connection, not an Identity
					return new X509HttpTafResp(access, null, "Certificate verified, but another Identity is presented", RESP.TRY_ANOTHER_TAF);
				}
				cert = certarr[0];
				responseText = ", validated by Mutual SSL Protocol";
			} else {		 // If cert == null, Get Declared Cert (in header), but validate by having them sign something
				if(authHeader != null && authHeader.startsWith("x509 ")) {
					ByteArrayOutputStream baos = new ByteArrayOutputStream(authHeader.length());
					try {
						array = authHeader.getBytes();
						ByteArrayInputStream bais = new ByteArrayInputStream(array);
						Symm.base64noSplit.decode(bais, baos, 5);
						certBytes = baos.toByteArray();
						cert = getCert(certBytes);
						
						/** 
						 * Identity from CERT if well know CA and specific encoded informatino
						 */
						// If found Identity doesn't work, try SignedStuff Protocol
//									cert.checkValidity();
//									cert.--- GET FINGERPRINT?
						String stuff = req.getHeader("Signature");
						if(stuff==null) 
							return new X509HttpTafResp(access, null, "Header entry 'Signature' required to validate One way X509 Certificate", RESP.TRY_ANOTHER_TAF);
						String data = req.getHeader("Data"); 
//									if(data==null) 
//										return new X509HttpTafResp(access, null, "No signed Data to validate with X509 Certificate", RESP.TRY_ANOTHER_TAF);

						// Note: Data Pos shows is "<signatureType> <data>"
//									int dataPos = (stuff.indexOf(' ')); // determine what is Algorithm
						// Get Signature 
						bais = new ByteArrayInputStream(stuff.getBytes());
						baos = new ByteArrayOutputStream(stuff.length());
						Symm.base64noSplit.decode(bais, baos);
						array = baos.toByteArray();
//									Signature sig = Signature.getInstance(stuff.substring(0, dataPos)); // get Algorithm from first part of Signature
						
						Signature sig = Signature.getInstance(cert.getSigAlgName()); 
						sig.initVerify(cert.getPublicKey());
						sig.update(data.getBytes());
						if(!sig.verify(array)) {
							access.log(Level.ERROR, "Signature doesn't Match");
							return new X509HttpTafResp(access, null, "Certificate NOT verified", RESP.TRY_ANOTHER_TAF);
						}
						responseText = ", validated by Signed Data";
					} catch (Exception e) {
						access.log(e, "Exception while validating Cert");
						return new X509HttpTafResp(access, null, "Certificate NOT verified", RESP.TRY_ANOTHER_TAF);
					}
					
				} else {
					return new X509HttpTafResp(access, null, "Not Certificate Info on Transaction", RESP.TRY_ANOTHER_TAF);
				}
			}

			// A cert has been found, match Identify
			Principal prin=null;
			
			for(int i=0;prin==null && i<certIdents.length;++i) {
				if((prin=certIdents[i].identity(req, cert, certBytes))!=null) {
					responseText = prin.getName() + " matches Certificate " + cert.getSubjectX500Principal().getName() + responseText;
//					xresp = new X509HttpTafResp(
//								access,
//								prin,
//								prin.getName() + " matches Certificate " + cert.getSubjectX500Principal().getName() + responseText,
//								RESP.IS_AUTHENTICATED);
					
				}
			}

			// if Principal is found, check for "AS_USER" and whether this entity is trusted to declare
			if(prin!=null) {
				String as_user=req.getHeader(Config.CADI_AS_USER);
				if(as_user!=null) {
					if(as_user.startsWith("TGUARD ") && lur.fish(prin, new LocalPermission("com.att.aaf.trust|"+prin.getName()+"|tguard"))) {
						prin = new TGuardPrincipal(as_user.substring(7));
						responseText=prin.getName() + " set via trust of " + responseText;
					}
				}
				return new X509HttpTafResp(
					access,
					prin,
					responseText,
					RESP.IS_AUTHENTICATED);
			}
		} catch(Exception e) {
			return new X509HttpTafResp(access, null, e.getMessage(), RESP.TRY_ANOTHER_TAF);	
		}
	
		return new X509HttpTafResp(access, null, "Certificate NOT verified", RESP.TRY_ANOTHER_TAF);
	}

	public Resp revalidate(CachedPrincipal prin) {
		return null;
	}

}
