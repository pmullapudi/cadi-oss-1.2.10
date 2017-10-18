/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.wsse.test;

import java.io.FileInputStream;

import javax.xml.stream.events.XMLEvent;

import org.junit.Test;

import com.att.cadi.wsse.XEvent;
import com.att.cadi.wsse.XReader;

public class JU_XReader {
	@Test
	public void test() throws Exception {
		FileInputStream fis = new FileInputStream("test/CBUSevent.xml");
		try {
			XReader xr = new XReader(fis);
			while(xr.hasNext()) {
				XEvent xe = xr.nextEvent();
				switch(xe.getEventType()) {
					case XMLEvent.START_DOCUMENT:
						System.out.println("Start Document");
						break;
					case XMLEvent.START_ELEMENT:
						System.out.println("Start Event: " + xe.asStartElement().getName());
						break;
					case XMLEvent.END_ELEMENT:
						System.out.println("End Event: " + xe.asEndElement().getName());
						break;
					case XMLEvent.CHARACTERS:
						System.out.println("Characters: " + xe.asCharacters().getData());
						break;
					case XMLEvent.COMMENT:
						System.out.println("Comment: " + ((XEvent.Comment)xe).value);
						break;
				}
			}
		} finally {
			fis.close();
		}
		
	}

}
