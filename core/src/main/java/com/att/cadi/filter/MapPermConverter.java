/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;

import java.util.HashMap;
import java.util.Map;

public class MapPermConverter implements PermConverter {
	private HashMap<String,String> map;

	/**
	 * Create with colon separated name value pairs
	 *  i.e. teAdmin=com.att.myNS.myPerm|*|*:teUser=...
	 *  
	 * @param value
	 */
	public MapPermConverter() {
		map = new HashMap<String,String>();
	}

	/**
	 * use to instantiate entries 
	 * 
	 * @return
	 */
	public Map<String,String> map() {
		return map;
	}

	public String convert(String minimal) {
		String rv = map.get(minimal);
		return rv==null?minimal:rv;
	}

}
