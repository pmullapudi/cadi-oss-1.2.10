/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import com.att.cadi.Access;
import com.att.cadi.Symm;
import com.att.cadi.config.Config;
import com.att.inno.env.Decryptor;
import com.att.inno.env.Env;
import com.att.rosetta.env.RosettaEnv;

public class EnvAccess implements Access {
	private Env env;

	public EnvAccess(RosettaEnv env) {
		this.env = env;

		// Load Property Files
		String cpf = env.getProperty(Config.CADI_PROP_FILES);
		if(cpf!=null) {
			InputStream is = classLoader().getResourceAsStream(cpf);
			if(is==null) {
				try {
					is = new FileInputStream(cpf);
				} catch (FileNotFoundException e) {
					env.error().log("Cannot find",cpf,"in Classpath or Filepath");
				}
			}
			if(is!=null) {
				try {
					try {
						load(is);
					} finally {
						is.close();
					}
				} catch (IOException e) {
					env.error().log(e);
				}
			}
		}
		
		// Load a Decryptor
		if(env.getProperty(Config.CADI_KEYFILE)!=null) {
			final Symm s = Symm.obtain(this);
			env.set(new Decryptor() {
				private Symm symm = s;
				@Override
				public String decrypt(String encrypted) {
					try {
						return encrypted!=null
								? symm.depass(encrypted)
								: encrypted;
					} catch (IOException e) {
						return "";
					}
				}
			});
		}

		env.loadToSystemPropsStartsWith("AFT_","DME2");
	}

	@Override
	public void log(Level level, Object... elements) {
		switch(level) {
			case AUDIT:
				env.audit().log(elements);
				break;
			case DEBUG:
				env.debug().log(elements);
				break;
			case ERROR:
				env.error().log(elements);
				break;
			case INFO:
				env.info().log(elements);
				break;
			case INIT:
				env.init().log(elements);
				break;
			case WARN:
				env.warn().log(elements);
				break;
			default:
				break;
		}
		
	}

	@Override
	public void log(Exception e, Object... elements) {
		env.error().log(e,elements);
	}

	@Override
	public void setLogLevel(Level level) {
		// unused
	}

	@Override
	public ClassLoader classLoader() {
		return env.getClass().getClassLoader();
	}

	@Override
	public String getProperty(String string, String def) {
		return env.getProperty(string, def);
	}
	
	@Override
	public void load(InputStream is) throws IOException {
		Properties props = new Properties();
		props.load(is);
		for(Entry<Object, Object> es :props.entrySet()) {
			env.setProperty(es.getKey().toString(), es.getValue().toString());
		}
	}

	@Override
	public String decrypt(String encrypted, boolean anytext) throws IOException {
		return env.decryptor().decrypt(encrypted);
	}

}
