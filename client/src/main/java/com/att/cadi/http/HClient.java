/*******************************************************************************
 * Copyright (c) 2017 AT&T Intellectual Property. All rights reserved.
 *******************************************************************************/
package com.att.cadi.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;

import javax.servlet.http.HttpServletResponse;

import com.att.cadi.LocatorException;
import com.att.cadi.SecuritySetter;
import com.att.cadi.client.EClient;
import com.att.cadi.client.Future;
import com.att.cadi.client.Rcli;
import com.att.inno.env.APIException;
import com.att.inno.env.Data;
import com.att.inno.env.Data.TYPE;
import com.att.inno.env.util.Pool.Pooled;
import com.att.rosetta.env.RosettaDF;

/**
 * Low Level Http Client Mechanism. Chances are, you want the high level "HRcli"
 * for Rosetta Object Translation
 * 
 *
 */
public class HClient implements EClient<HttpURLConnection> {
	private URI uri;
	private ArrayList<Header> headers;
	private String meth;
	private String pathinfo;
	private String query;
	private String fragment;
	private Transfer transfer;
	private SecuritySetter<HttpURLConnection> ss;
	private HttpURLConnection huc;
	private int connectTimeout;

	public HClient(SecuritySetter<HttpURLConnection> ss, URI uri,
			int connectTimeout) throws Exception {
		if (uri == null)
			throw new LocatorException("No Client available to call");
		this.uri = uri;
		this.ss = ss;
		this.connectTimeout = connectTimeout;
		pathinfo = query = fragment = ""; 
	}

	@Override
	public void setMethod(String meth) {
		this.meth = meth;
	}

	@Override
	public void setPathInfo(String pathinfo) {
		this.pathinfo = pathinfo;
	}

	@Override
	public void setPayload(Transfer transfer) {
		this.transfer = transfer;
	}
	
	@Override
	public void addHeader(String tag, String value) {
		if (headers == null)
			headers = new ArrayList<Header>();
		headers.add(new Header(tag, value));
	}

	@Override
	public void setQueryParams(String q) {
		query = q;
	}

	@Override
	public void setFragment(String f) {
		fragment = f;
	}

	@Override
	public void send() throws APIException {
		try {
			// Build URL from given URI plus current Settings
			StringBuilder pi = new StringBuilder(uri.getPath());
			if(!pathinfo.startsWith("/"))
				pi.append('/');
			pi.append(pathinfo);
			URL url = new URI(
					uri.getScheme(), 
					uri.getUserInfo(),
					uri.getHost(), 
					uri.getPort(), 
					pi.toString(), 
					query,
					fragment).toURL();
			pathinfo=null;
			query=null;
			fragment=null;
			huc = (HttpURLConnection) url.openConnection();
			if(ss!=null) ss.setSecurity(huc);
			huc.setRequestMethod(meth);
			if (headers != null)
				for (Header d : headers) {
					huc.addRequestProperty(d.tag, d.value);
				}
			huc.setDoInput(true);
			huc.setDoOutput(true);
			huc.setUseCaches(false);
			huc.setConnectTimeout(connectTimeout);
			huc.connect();
			if (transfer != null) {
				transfer.transfer(huc.getOutputStream());
			}
			// TODO other settings? There's a bunch here.
		} catch (Exception e) {
			throw new APIException(e);
		} finally { // ensure all these are reset after sends
			meth=pathinfo=null;
			if(headers!=null)headers.clear();
			pathinfo = query = fragment = "";
		}
	}

	@Override
	public <T> Future<T> futureCreate(Class<T> t) {
		return new HFuture<T>(huc) {
			public boolean get(int timeout) throws Exception {
				try {
					huc.setReadTimeout((int) timeout);
					respMessage = huc.getResponseMessage();
					respCode = huc.getResponseCode();
					if(respCode == 201) {
						return true; 
					} else {
						extractError();
						return false;
					}
				} finally {
					close();
				}
			}

			@Override
			public String body() {
				if (errContent != null) {
					return errContent.toString();
	
				} else if (respMessage != null) {
					return respMessage;
				}
				return "";
			}

		};

	}

	@Override
	public Future<String> futureReadString() {
		return new HFuture<String>(huc) {
			public boolean get(int timeout) throws Exception {
				try {
					huc.setReadTimeout(timeout);
					respCode = huc.getResponseCode();
					respMessage = huc.getResponseMessage();
					if (respCode == 200) {
						StringBuilder sb = inputStreamToString(huc.getInputStream());
						if (sb != null) {
							value = sb.toString();
						}
						return true;
					} else {
						extractError();
					}
				} finally {
					close();
				}
				return false;

			}

			@Override
			public String body() {
				if (value != null) {
					return value;
				} else if (errContent != null) {
					return errContent.toString();
				} else if (respMessage != null) {
					return respMessage;
				}
				return "";
			}

		};
	}

	@Override
	public <T> Future<T> futureRead(final RosettaDF<T> df, final TYPE type) {
		return new HFuture<T>(huc) {
			private Data<T> data;

			public boolean get(int timeout) throws Exception {
				try {
					huc.setReadTimeout(timeout);
					respCode = huc.getResponseCode();
					respMessage = huc.getResponseMessage();
					if (respCode == 200) {
						data = df.newData().in(type).load(huc.getInputStream());
						value = data.asObject();
						return true;
					} else {
						extractError();
					}
				} finally {
					close();
				}
				return false;
			}

			@Override
			public String body() {
				if (data != null) {
					try {
						return data.asString();
					} catch (APIException e) {
					}
				} else if (errContent != null) {
					return errContent.toString();
				} else if (respMessage != null) {
					return respMessage;
				}
				return "";
			}
		};
	}

	@Override
	public <T> Future<T> future(final T t) {
		return new HFuture<T>(huc) {
			public boolean get(int timeout) throws Exception {
				try {
					huc.setReadTimeout(timeout);
					respCode = huc.getResponseCode();
					respMessage = huc.getResponseMessage();
					if (respCode == 200) {
						value = t;
						return true;
					} else {
						extractError();
					}
				} finally {
					close();
				}
				return false;
			}

			@Override
			public String body() {
				if (errContent != null) {
					return errContent.toString();
				} else if (respMessage != null) {
					return respMessage;
				}
				return Integer.toString(respCode);
			}
		};
	}

	@Override
	public Future<Void> future(final HttpServletResponse resp, final int expected) throws APIException {
		return new HFuture<Void>(huc) {
			public boolean get(int timeout) throws Exception {
				try {
					huc.setReadTimeout(timeout);
					resp.setStatus(respCode=huc.getResponseCode());
					respMessage = huc.getResponseMessage();
					int read;
					InputStream is;
					OutputStream os = resp.getOutputStream();
					if(respCode==expected) {
						is = huc.getInputStream();
						// reuse Buffers
						Pooled<byte[]> pbuff = Rcli.buffPool.get();
						try { 
							while((read=is.read(pbuff.content))>=0) {
								os.write(pbuff.content,0,read);
							}
						} finally {
							pbuff.done();
						}
						return true;
					} else {
						is = huc.getErrorStream();
						try {
							if(is==null) {
								is = huc.getInputStream();
							}
							if(is!=null) {
								errContent = new StringBuilder();
								Pooled<byte[]> pbuff = Rcli.buffPool.get();
								try { 
									while((read=is.read(pbuff.content))>=0) {
										os.write(pbuff.content,0,read);
									}
								} finally {
									pbuff.done();
								}
							}
						} catch (IOException e) {
							exception = e;
						}
					}
					return false;
				} finally {
					close();
				}
			}

			@Override
			public String body() {
				return errContent==null?respMessage:errContent.toString();
			}
		};
	}

	public abstract class HFuture<T> extends Future<T> {
		protected HttpURLConnection huc;
		protected int respCode;
		protected String respMessage;
		protected IOException exception;
		protected StringBuilder errContent;

		public HFuture(final HttpURLConnection huc) {
			this.huc = huc;
		}

		@Override
		public boolean get(int timeout) throws Exception {
			try {
				huc.setReadTimeout(timeout);
				respCode = huc.getResponseCode();
				respMessage = huc.getResponseMessage();
			} finally {
				close();
			}
			return respCode == 200;
		}

		protected void extractError() {
			InputStream is = huc.getErrorStream();
			try {
				if(is==null) {
					is = huc.getInputStream();
				}
				if(is!=null) {
				errContent = new StringBuilder();
				int c;
					while((c=is.read())>=0) {
						errContent.append((char)c);
					}
				}
			} catch (IOException e) {
				exception = e;
			}
		}

		// Typically only used by Read
		public StringBuilder inputStreamToString(InputStream is) {
			// Avoids Carriage returns, and is reasonably efficient, given
			// the buffer reads.
			try {
				StringBuilder sb = new StringBuilder();
				Reader rdr = new InputStreamReader(is);
				try {
					char[] buf = new char[256];
					int read;
					while ((read = rdr.read(buf)) >= 0) {
						sb.append(buf, 0, read);
					}
				} finally {
					rdr.close();
				}
				return sb;
			} catch (IOException e) {
				exception = e;
				return null;
			}
		}


		@Override
		public int code() {
			return respCode;
		}

		public HttpURLConnection huc() {
			return huc;
		}

		public IOException exception() {
			return exception;
		}

		public String respMessage() {
			return respMessage;
		}

		@Override
		public String header(String tag) {
			return huc.getHeaderField(tag);
		}

		public void close() {
			huc.disconnect();
		}
	}

	private static class Header {
		public final String tag;
		public final String value;

		public Header(String t, String v) {
			this.tag = t;
			this.value = v;
		}
		
		public String toString() {
			return tag + '=' + value;
		}
	}

}
