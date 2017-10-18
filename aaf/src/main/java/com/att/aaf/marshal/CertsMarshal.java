/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.marshal;

import java.util.List;

import aaf.v2_0.Certs;
import aaf.v2_0.Certs.Cert;

import com.att.rosetta.marshal.ObjArray;
import com.att.rosetta.marshal.ObjMarshal;

public class CertsMarshal extends ObjMarshal<Certs> {

	public CertsMarshal() {
		add(new ObjArray<Certs,Cert>("cert",new CertMarshal()) {
			@Override
			protected List<Cert> data(Certs t) {
				return t.getCert();
			}
		});	
	}


}
