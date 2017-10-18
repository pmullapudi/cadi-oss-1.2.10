/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
/**
 * RolesAllowed 
 * 
 * @author jg1555
 * 
 * Similar to Java EE's Spec from Annotations 1.1, 2.8
 * 
 * That Spec, however, was geared towards being able to route calls to Methods on Objects, and thus needed a more refined
 * sense of permissions hierarchy.  The same mechanism, however, can easily be achieved on single Servlet/Handlers in
 * POJOs like Jetty by simply adding the Roles Allowed in a similar Annotation
 * 
 */
package com.att.cadi.filter;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * JASPI Style Annotation of RolesAllowed when the coding style is desired but actually including all 
 * JEE jars is not. If using actual JASPI, use official @interface classes, not this one...
 * 
 * @author jg1555
 */
@Target({TYPE})
@Retention(RUNTIME)
public @interface RolesAllowed {
	/**
	 * Security role of the implementation, which doesn't have to be an EJB or CORBA like object.  Can be just a
	 * Handler
	 * @return
	 */
	String[] value();
}
