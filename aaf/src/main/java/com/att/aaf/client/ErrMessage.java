/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.aaf.client;

import java.io.PrintStream;

import aaf.v2_0.Error;

import com.att.inno.env.APIException;
import com.att.inno.env.Data.TYPE;
import com.att.rosetta.env.RosettaDF;
import com.att.rosetta.env.RosettaEnv;

public class ErrMessage {
	private RosettaDF<Error> errDF;
	
	public ErrMessage(RosettaEnv env) throws APIException {
		errDF = env.newDataFactory(Error.class);
	}

	/**
	 * AT&T Requires a specific Error Format for RESTful Services, which AAF complies with.
	 * 
	 * This code will create a meaningful string from this format. 
	 * 
	 * @param ps
	 * @param df
	 * @param r
	 * @throws APIException
	 */
	public void printErr(PrintStream ps,  String attErrJson) throws APIException {
		Error err = errDF.newData().in(TYPE.JSON).load(attErrJson).asObject();
		ps.append(err.getMessageId());
		final String text = err.getText();
		int arrSize = err.getVariables().size();
		char c;
		StringBuilder num = null;
		for(int i=0;i<text.length();++i) {
			if((c=text.charAt(i))=='%') {
				if(num==null) {
					num = new StringBuilder();
				} else {
					num.setLength(0);
				}
				while(++i < text.length() && Character.isDigit(c=text.charAt(i))) {
					num.append(c);
				}
				int idx = Integer.parseInt(num.toString())-1;
				if(idx>=0 && idx<arrSize) {
					ps.append(err.getVariables().get(idx));
				}
				ps.append(c);
			} else {
				ps.append(c);
			}
		}
		ps.println();
	}
	
	/**
	 * AT&T Requires a specific Error Format for RESTful Services, which AAF complies with.
	 * 
	 * This code will create a meaningful string from this format. 
	 * 
	 * @param sb
	 * @param df
	 * @param r
	 * @throws APIException
	 */
	public void printErr(StringBuilder sb,  String attErrJson) throws APIException {
		Error err = errDF.newData().in(TYPE.JSON).load(attErrJson).asObject();
		sb.append(err.getMessageId());
		final String text = err.getText();
		int arrSize = err.getVariables().size();
		char c;
		StringBuilder num = null;
		for(int i=0;i<text.length();++i) {
			if((c=text.charAt(i))=='%') {
				if(num==null) {
					num = new StringBuilder();
				} else {
					num.setLength(0);
				}
				while(++i < text.length() && Character.isDigit(c=text.charAt(i))) {
					num.append(c);
				}
				int idx = Integer.parseInt(num.toString())-1;
				if(idx>=0 && idx<arrSize) {
					sb.append(err.getVariables().get(idx));
				}
				sb.append(c);
			} else {
				sb.append(c);
			}
		}
	}

}
