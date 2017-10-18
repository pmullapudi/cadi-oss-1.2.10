/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.filter;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.servlet.Servlet;

@Target({TYPE})
@Retention(RUNTIME)
public @interface AUTHZ {
	Class<? extends Servlet> value();
}
