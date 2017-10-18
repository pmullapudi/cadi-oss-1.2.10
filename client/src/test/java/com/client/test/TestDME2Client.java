/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.client.test;

import java.io.FileInputStream;
import java.net.URI;
import java.util.Properties;

import com.att.aft.dme2.api.DME2Client;
import com.att.aft.dme2.api.DME2Manager;
import com.att.cadi.Symm;

public class TestDME2Client {
	public static void main(String[] args) {
		try {
			Properties props = System.getProperties();
			props.put("AFT_LATITUDE","32.780140");
			props.put("AFT_LONGITUDE","-96.800451");
			props.put("AFT_ENVIRONMENT","AFTUAT");
			
			//props.put("keyStore","/Volumes/Data/src/authz/common/aaf.att.jks");
			props.put("AFT_DME2_KEYSTORE","/Volumes/Data/src/authz/common/aaf.att.jks");
			props.put("AFT_DME2_KEYSTORE_PASSWORD","enc:EH1j5kyfzwLj5b3CUwbaxWZbuQbgmsnHuw");
			props.put("AFT_DME2_TRUSTSTORE","/Volumes/Data/src/authz/common/truststore.jks");
			props.put("AFT_DME2_TRUSTSTORE_PASSWORD","enc:xE3HnptUe5xgW5JFiJntYQtMuwpgzsRmxWUHAIfI");
			
			// Local Testing on dynamic IP PC ***ONLY***
//			props.put("DME2_EP_REGISTRY_CLASS","DME2FS");
//			props.put("AFT_DME2_EP_REGISTRY_FS_DIR","/Volumes/Data/src/authz/dme2reg");
//			props.put("AFT_DME2_SSL_TRUST_ALL", "true");

			Symm symm;
			FileInputStream keyfile=new FileInputStream("/Volumes/Data/src/authz/common/keyfile");
			try {
				symm=Symm.obtain(keyfile);
			} finally {
				keyfile.close();
			}

			DME2Manager dm = new DME2Manager("DME2Manager TestHClient",props);
					  // Standard RESOLVE format
			String prefix;
			URI uri = 
//					new URI(
//					  "https://DME2RESOLVE/service=com.att.authz.AuthorizationService/version=2.0/envContext=DEV/routeOffer=BAU_SE"
//					);
//				prefix = "";
//				   Direct Format
//				   new URI("https://mithrilcsp.sbc.com:8100/service=com.att.authz.AuthorizationService/version=2.0/envContext=DEV/routeOffer=BAU_SE");
//				   prefix = "";
//				   Go through PROXY
//			   	   new URI("https://mithrilcsp.sbc.com:8095");
//				   prefix = "/proxy";
					
//					new URI("https://mithrilcsp.sbc.com:8095");
					new URI("https://DME2RESOLVE/service=com.att.authz.authz-gw/version=2.0/envContext=UAT/routeOffer=BAU_SE");
//				   prefix = "";
				   prefix = "/proxy";
			DME2Client client = new DME2Client(dm,uri,3000);

			client.setCredentials("m12345@aaf.att.com", symm.depass("enc:bFuSqQOUoydwlsAKil_CDstMu8AaMs_KmOUbDWMq"));
			
			client.addHeader("Accept", "text/plain");
			client.setMethod("GET");
			client.setContext(prefix+"/authn/basicAuth");
			client.setPayload("");// Note: Even on "GET", you need a String in DME2
			
			String o = client.sendAndWait(5000); // There are other Asynchronous call options, see DME2 Docs
			
			System.out.println('[' + o + ']' + " (blank is good)");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
