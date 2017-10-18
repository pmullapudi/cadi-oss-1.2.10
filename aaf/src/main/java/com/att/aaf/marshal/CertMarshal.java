/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.marshal;

import javax.xml.datatype.XMLGregorianCalendar;

import aaf.v2_0.Certs.Cert;

import com.att.rosetta.marshal.FieldDateTime;
import com.att.rosetta.marshal.FieldHexBinary;
import com.att.rosetta.marshal.FieldString;
import com.att.rosetta.marshal.ObjMarshal;

public class CertMarshal extends ObjMarshal<Cert> {
	public CertMarshal() {
		add(new FieldHexBinary<Cert>("fingerprint") {
			@Override
			protected byte[] data(Cert t) {
				return t.getFingerprint();
			}
		});

		add(new FieldString<Cert>("id") {
			@Override
			protected String data(Cert t) {
				return t.getId();
			}
		});

		add(new FieldString<Cert>("x500") {
			@Override
			protected String data(Cert t) {
				return t.getX500();
			}
		});
		
		add(new FieldDateTime<Cert>("expires") {
			@Override
			protected XMLGregorianCalendar data(Cert t) {
				return t.getExpires();
			}
		});


	}
}
