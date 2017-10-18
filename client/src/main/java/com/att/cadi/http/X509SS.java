/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.X509KeyManager;

import com.att.cadi.CadiException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;
import com.att.cadi.config.SecurityInfo;
import com.att.inno.env.APIException;
import com.att.inno.env.util.Chrono;


public class X509SS implements SecuritySetter<HttpURLConnection> {
	private static final byte[] X509 = "x509 ".getBytes();
	private PrivateKey priv;
	private byte[] pub;
	private String cert;
	private SecurityInfo securityInfo;
	private String algo;
	private static int count = new Random().nextInt();

	public X509SS(SecurityInfo si) throws APIException, IOException, CertificateEncodingException {
		this(si,null);
	}
	public X509SS(SecurityInfo si, String alias) throws APIException, IOException, CertificateEncodingException {
		securityInfo = si;
		if(alias == null) alias = si.default_alias;
		if(alias == null) throw new APIException("JKS Alias is required to use X509SS Security.  Use " + Config.CADI_ALIAS +" to set default alias");
		priv=null;
		X509KeyManager[] xkms = si.getKeyManagers();
		if(xkms==null) {
			System.out.println("need KeyManager");
			System.exit(1);
		}
		for(int i=0;priv==null&&i<xkms.length;++i) {
			priv = xkms[i].getPrivateKey(alias);
		}
		for(int i=0;cert==null&&i<xkms.length;++i) {
			X509Certificate[] chain = xkms[i].getCertificateChain(alias);
			if(chain!=null&&chain.length>0) {
				algo = chain[0].getSigAlgName(); 
				pub = chain[0].getEncoded();
				ByteArrayOutputStream baos = new ByteArrayOutputStream(pub.length*2); 
				ByteArrayInputStream bais = new ByteArrayInputStream(pub);
				Symm.base64noSplit().encode(bais,baos,X509);
				cert = baos.toString();
				
				/*
				// Test
				bais = new ByteArrayInputStream(baos.toByteArray());
				baos = new ByteArrayOutputStream(input.length*2);
				Symm.base64noSplit().decode(bais,baos,5);
				byte[] output = baos.toByteArray();
				String reconstitute = output.toString();
				System.out.println("ok");
				CertificateFactory certFactory;
				try {
					bais = new ByteArrayInputStream(output);
					certFactory = CertificateFactory.getInstance("X.509");
					X509Certificate x509 = (X509Certificate)certFactory.generateCertificate(bais);
					System.out.println(x509.toString());
				} catch (CertificateException e) {
					e.printStackTrace();
				}
				*/
			}
		}
		if(algo==null) throw new APIException("X509 Security Setter not configured");
	}

	@Override
	public void setSecurity(HttpURLConnection huc) throws CadiException {
		if(huc instanceof HttpsURLConnection) {
			securityInfo.setSocketFactoryOn((HttpsURLConnection)huc);
		}
		if(securityInfo.isOneway() ) {
			huc.setRequestProperty("Authorization", cert);
			
			// Test Signed content
			try {
				String data = "SignedContent["+ inc() + ']' + Chrono.dateTime();
				huc.setRequestProperty("Data", data);
				
				Signature sig = Signature.getInstance(algo);
				sig.initSign(priv);
				sig.update(data.getBytes());
				byte[] signature = sig.sign();
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream((int)(signature.length*1.3));
				ByteArrayInputStream bais = new ByteArrayInputStream(signature);
				Symm.base64noSplit().encode(bais, baos);
				huc.setRequestProperty("Signature", new String(baos.toByteArray()));
				
			} catch (Exception e) {
				throw new CadiException(e);
			}
		}
	}
	
	private synchronized int inc() {
		return ++count;
	}

}
