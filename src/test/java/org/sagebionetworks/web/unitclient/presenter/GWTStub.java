package org.sagebionetworks.web.unitclient.presenter;import java.io.UnsupportedEncodingException;import java.net.URLDecoder;import java.net.URLEncoder;import org.sagebionetworks.web.client.GWTWrapper;import org.sagebionetworks.web.client.utils.Callback;import com.google.gwt.i18n.client.DateTimeFormat;import com.google.gwt.i18n.client.NumberFormat;import com.google.gwt.xhr.client.XMLHttpRequest;public class GWTStub implements GWTWrapper {	public GWTStub() {	}	@Override	public String getHostPageBaseURL() {		return "http://hostpage/url";	}	@Override	public String getModuleBaseURL() {		return "http://baseurl/";	}	@Override	public void assignThisWindowWith(String url) {	}	@Override	public String encodeQueryString(String queryString) {		return URLEncoder.encode(queryString);	}	@Override	public String decodeQueryString(String queryString) {		return URLDecoder.decode(queryString);	}	@Override	public XMLHttpRequest createXMLHttpRequest() {		return null;	}	@Override	public NumberFormat getNumberFormat(String pattern) {		return null;	}	@Override	public String getHostPrefix() {		return null;	}	@Override	public String getCurrentURL() {		return null;	}	@Override	public DateTimeFormat getDateTimeFormat() {		return null;	}	@Override	public void scheduleExecution(Callback callback, int delay) {			}	@Override	public String getUserAgent() {		return null;	}	@Override	public String getAppVersion() {		return null;	}	@Override	public String urlEncode(String toEncode) {		try {			return URLEncoder.encode(toEncode, "UTF-8");		} catch (UnsupportedEncodingException e) {			throw new RuntimeException(e);		}	}	@Override	public String urlDecode(String toDecode) {		try {			return URLDecoder.decode(toDecode, "UTF-8");		} catch (UnsupportedEncodingException e) {			throw new RuntimeException(e);		}	}}