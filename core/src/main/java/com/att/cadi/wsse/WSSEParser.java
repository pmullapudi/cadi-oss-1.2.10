/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.wsse;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLStreamException;

import com.att.cadi.BasicCred;


/**
 * WSSE Parser
 * 
 * Read the User and Password from WSSE Formatted SOAP Messages 
 * 
 * This class uses StAX so that processing is stopped as soon as the Security User/Password are read into BasicCred, or the Header Ends
 * 
 * This class is intended to be created once (or very few times) and reused as much as possible.
 * 
 * It is as thread safe as StAX parsing is.
 * 
 * @author jg1555
 */
@SuppressWarnings({ "unchecked" })
public class WSSEParser {
	private static final String SOAP_NS = "http://schemas.xmlsoap.org/soap/envelope/";
	private static final String WSSE_NS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private Match<BasicCred> parseTree;
	//private XMLInputFactory inputFactory;

	public WSSEParser() {
		// soap:Envelope/soap:Header/wsse:Security/wsse:UsernameToken/[wsse:Password&wsse:Username]
		parseTree = new Match<BasicCred>(SOAP_NS,"root", // need a root level to start from... Doesn't matter what the tag is
			new Match<BasicCred>(SOAP_NS,"Envelope",
				new Match<BasicCred>(SOAP_NS,"Header",
					new Match<BasicCred>(WSSE_NS,"Security",
						new Match<BasicCred>(WSSE_NS,"UsernameToken",
							new Match<BasicCred>(WSSE_NS,"Password").set(new Action<BasicCred>() {
								public boolean content(BasicCred bc,String text) {
									bc.setCred(text.getBytes());
									return true;
								}
							}),
							new Match<BasicCred>(WSSE_NS,"Username").set(new Action<BasicCred>() {
								public boolean content(BasicCred bc,String text) {
									bc.setUser(text);
									return true;
								}
							})
						).stopAfter() // if found, end when UsernameToken ends (no further processing needed)
					)
				).stopAfter() // Stop Processing when Header Ends
			).exclusive()// Envelope must match Header, and no other.  FYI, Body comes after Header short circuits (see above), so it's ok
		).exclusive(); // root must be Envelope
		//inputFactory = XMLInputFactory.newInstance();
	}
	
	public XMLStreamException parse(BasicCred bc, InputStream is) throws IOException {
		try {
			parseTree.onMatch(bc, new XReader(is));
			return null;
		} catch (XMLStreamException e) {
			return e;
		}
	}
}
