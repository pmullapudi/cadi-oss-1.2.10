/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi;

import com.att.cadi.taf.TafResp;


/**
 * TAF - Transmutative Assertion Framework.  
 * 
 * This main Interface embodies the essential of the assertion, where a number of different TAFs might be used to authenticate
 * and that authentication to be recognized through other elements.
 * 
 * Concept by Robert Garskof.  Implemented by Jonathan Gathman
 *  
 * @author jg1555
 *
 */
public interface Taf {
	enum LifeForm {CBLF, SBLF, LFN};
	/**
	 * The lifeForm param is a humorous way of describing whether the interaction is proceeding from direct Human Interaction via a browser 
	 * or App which can directly query a memorized password, key sequence, bio-feedback, from that user, or a machine mechanism for which identity
	 * can more easily be determined by Certificate, Mechanical ID/Password etc.  Popularized in modern culture and Science Fiction (especially 
	 * Star Trek), we (starting with Robert Garskof) use the terms "Carbon Based Life Form" (CBLF) for mechanisms with people at the end of them, or 
	 * "Silicon Based Life Forms" (SBLF) to indicate machine only interactions.  I have added "LFN" for (Life-Form Neutral) to aid identifying
	 * processes for which it doesn't matter whether there is a human at the immediate end of the chain, or cannot be determined mechanically.  
	 * 
	 * The variable parameter is not necessarily ideal, but with too many unknown Tafs to be created, flexibility,
	 * is unfortunately required at this point.  Future versions could lock this down more.  JG 10/18/2012
	 * 
	 * @param lifeForm
	 * @param info
	 * @return
	 */
	public TafResp validate(LifeForm reading, String ... info);
	
}
