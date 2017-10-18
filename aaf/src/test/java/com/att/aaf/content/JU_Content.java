/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.content;

import java.io.StringReader;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import aaf.v2_0.Error;

import com.att.rosetta.env.RosettaDF;
import com.att.rosetta.env.RosettaData;
import com.att.rosetta.env.RosettaEnv;

public class JU_Content {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}


	@Test
	public void parseErrorJSON() throws Exception {
		final String msg = "{\"messageId\":\"SVC2000\",\"text\":\"Select which cred to delete (or 0 to delete all):" +
			"1) %1" +
			"2) %2" +
			"3) %3" +
			"4) %4" +
			"Run same command again with chosen entry as last parameter\"," +
			"\"variables\":[" +
			"\"m55555@jr583u.cred.test.com 1 Wed Oct 08 11:48:08 CDT 2014\"," +
			"\"m55555@jr583u.cred.test.com 1 Thu Oct 09 12:54:46 CDT 2014\"," +
			"\"m55555@jr583u.cred.test.com 1 Tue Jan 06 05:00:00 CST 2015\"," +
			"\"m55555@jr583u.cred.test.com 1 Wed Jan 07 05:00:00 CST 2015\"]}";
		
		Error err = new Error();
		err.setText("Hello");
		err.getVariables().add("I'm a teapot");
		err.setMessageId("12");
		
		
//		System.out.println(msg);
		RosettaEnv env = new RosettaEnv();
		RosettaDF<aaf.v2_0.Error> errDF = env.newDataFactory(aaf.v2_0.Error.class);
		errDF.in(RosettaData.TYPE.JSON);
		errDF.out(RosettaData.TYPE.JSON);
		RosettaData<Error> data = errDF.newData();
		data.load(err);
		System.out.println(data.asString());
		
		data.load(new StringReader(msg));
		err = data.asObject();
		System.out.println(err.getText());
	}
		

}
