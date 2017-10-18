/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.test;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.Test;

import com.att.cadi.util.Vars;

public class JU_Vars {

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() {
		StringBuilder holder = new StringBuilder();
		String str,bstr;
		assertEquals(str = "set %1 to %2",Vars.convert(holder,str, "a","b"));
		assertEquals("set a to b",holder.toString());
		assertEquals(str,Vars.convert(null,str, "a","b"));
		holder.setLength(0);
		assertEquals(str,Vars.convert(holder,bstr="set %s to %s", "a","b"));
		assertEquals("set a to b",holder.toString());
		assertEquals(str,Vars.convert(null,bstr, "a","b"));
		
		holder.setLength(0);
		assertEquals(str = "%1=%2",Vars.convert(holder,str, "a","b"));
		assertEquals("a=b",holder.toString());
		assertEquals(str,Vars.convert(null,str, "a","b"));
		holder.setLength(0);
		assertEquals(str,Vars.convert(holder,bstr="%s=%s", "a","b"));
		assertEquals("a=b",holder.toString());
		assertEquals(str,Vars.convert(null,bstr, "a","b"));
		
		holder.setLength(0);
		assertEquals(str = "%1%2",Vars.convert(holder,str, "a","b"));
		assertEquals("ab",holder.toString());
		assertEquals(str ,Vars.convert(null,str, "a","b"));
		holder.setLength(0);
		assertEquals(str,Vars.convert(holder,bstr="%s%s", "a","b"));
		assertEquals("ab",holder.toString());
		assertEquals(str ,Vars.convert(null,bstr, "a","b"));


		holder.setLength(0);
		assertEquals(str = " %1=%2 ",Vars.convert(holder,str, "a","b"));
		assertEquals(" a=b ",holder.toString());
		assertEquals(str ,Vars.convert(null,str, "a","b"));
		holder.setLength(0);
		assertEquals(str,Vars.convert(holder,bstr = " %s=%s ", "a","b"));
		assertEquals(" a=b ",holder.toString());
		assertEquals(str ,Vars.convert(null,bstr, "a","b"));

		holder.setLength(0);
		assertEquals(str = " %1%2%10 ",Vars.convert(holder,str, "a","b","c","d","e","f","g","h","i","j"));
		assertEquals(" abj ",holder.toString());
		assertEquals(str,Vars.convert(null,str, "a","b","c","d","e","f","g","h","i","j"));
		holder.setLength(0);
		assertEquals(str=" %1%2%3 ",Vars.convert(holder,bstr = " %s%s%s ", "a","b","c","d","e","f","g","h","i","j"));
		assertEquals(" abc ",holder.toString());
		assertEquals(str,Vars.convert(null,bstr, "a","b","c","d","e","f","g","h","i","j"));
		

		holder.setLength(0);
		assertEquals(str = "set %1 to %2",Vars.convert(holder,str, "Something much","larger"));
		assertEquals("set Something much to larger",holder.toString());
		assertEquals(str,Vars.convert(null,str,"Something much","larger"));
		holder.setLength(0);
		assertEquals(str,Vars.convert(holder,bstr="set %s to %s", "Something much","larger"));
		assertEquals("set Something much to larger",holder.toString());
		assertEquals(str,Vars.convert(null,bstr, "Something much","larger"));

		holder.setLength(0);
		assertEquals(str = "Text without Vars",Vars.convert(holder,str));
		assertEquals(str,holder.toString());
		assertEquals(str = "Text without Vars",Vars.convert(null,str));
	
		
		holder.setLength(0);
		assertEquals(str = "Not %1 Enough %2 Vars %3",Vars.convert(holder,str, "a","b"));
		assertEquals("Not a Enough b Vars ",holder.toString());
		assertEquals(str ,Vars.convert(null,str, "a","b"));
		holder.setLength(0);
		assertEquals(str,Vars.convert(holder,bstr="Not %s Enough %s Vars %s", "a","b"));
		assertEquals("Not a Enough b Vars ",holder.toString());
		assertEquals(str ,Vars.convert(null,bstr, "a","b"));

		holder.setLength(0);
		assertEquals(str = "!@#$%^*()-+?/,:;.",Vars.convert(holder,str, "a","b"));
		assertEquals(str,holder.toString());
		assertEquals(str ,Vars.convert(null,str, "a","b"));

		holder.setLength(0);
		bstr = "%s !@#$%^*()-+?/,:;.";
		str = "%1 !@#$%^*()-+?/,:;.";
		assertEquals(str,Vars.convert(holder,bstr, "Not Acceptable"));
		assertEquals("Not Acceptable !@#$%^*()-+?/,:;.",holder.toString());
		assertEquals(str ,Vars.convert(null,bstr, "Not Acceptable"));

	}

}
