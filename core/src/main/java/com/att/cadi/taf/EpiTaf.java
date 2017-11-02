/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.taf;

import com.att.cadi.CadiException;
import com.att.cadi.Taf;

/**
 * EpiTAF
 * 
 * Short for "Epic TAF". Be able to run through a series of TAFs to obtain the validation needed.
 * 
 * OK, the name could probably be better as "Tafs", like it was originally, but the pun was too
 * irresistible for this author to pass up.
 * 
 *
 */
public class EpiTaf implements Taf {
	private Taf[] tafs;
	
	/**
	 * EpiTaf constructor
	 * 
	 * Construct the EpiTaf from variable TAF parameters
	 * @param tafs
	 * @throws CadiException
	 */
	public EpiTaf(Taf ... tafs) throws CadiException{
		this.tafs = tafs;
		if(tafs.length==0) throw new CadiException("Need at least one Taf implementation in constructor");
	}

	/**
	 * validate
	 * 
	 * Respond with the first TAF to authenticate user based on variable info and "LifeForm" (is it 
	 * a human behind an interface, or a server behind a protocol).
	 * 
	 * If there is no TAF that can authenticate, respond with the first TAF that suggests it can
	 * establish an Authentication conversation (TRY_AUTHENTICATING).
	 * 
	 * If no TAF declares either, respond with NullTafResp (which denies all questions)
	 */
	public TafResp validate(LifeForm reading, String... info) {
		TafResp tresp,firstTryAuth=null;
		for(Taf taf : tafs) {
			tresp = taf.validate(reading, info);
			switch(tresp.isAuthenticated()) {
				case TRY_ANOTHER_TAF:
					break;
				case TRY_AUTHENTICATING:
					if(firstTryAuth==null)firstTryAuth=tresp;
					break;
				default:
					return tresp;
			}
		}

		// No TAFs configured, at this point.  It is safer at this point to be "not validated", 
		// rather than "let it go"
		return firstTryAuth == null?NullTafResp.singleton():firstTryAuth;
	}

}
