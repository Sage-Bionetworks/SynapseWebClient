package org.sagebionetworks.web.client.cookie;

import java.util.Collection;
import java.util.Date;

import org.sagebionetworks.web.client.DisplayUtils;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

/**
 * A simple wrapper of the GWT cookie implementation.
 * 
 * @author jmhill
 *
 */
public class GWTCookieImpl implements CookieProvider {
	
	@Override
	public String getCookie(String name) {
		return Cookies.getCookie(name);
	}

	@Override
	public Collection<String> getCookieNames() {
		return Cookies.getCookieNames();
	}

	@Override
	public void removeCookie(String key) {
		setCookie(key, "", new Date());
	}

	@Override
	public void setCookie(String name, String value) {
		setCookie(name, value, null);
	}

	@Override
	public void setCookie(String name, String value, Date expires) {
		String domain = CookieUtils.getDomain(Window.Location.getHostName());
		boolean isSecure = domain != null;
		setCookie(name, value, expires, domain, null, isSecure);
	}
	
	@Override
	public void setCookie(String name, String value, Date expires, String domain, String path, boolean secure) {
		double expiresTime = (expires == null) ? 0 : expires.getTime();
		boolean httpOnly = false;
		_setCookie(name, value, expiresTime, domain, path, secure, httpOnly);
	}

	private static native void _setCookie(String name, String value, double expires, String domain, String path, boolean secure, boolean httpOnly) /*-{
	  var c = name + '=' + value;
	  if ( expires )
	    c += ';expires=' + (new Date(expires)).toGMTString();
	  if (domain)
	    c += ';domain=' + domain;
	  if (path)
	    c += ';path=' + path;
	  if (secure)
	    c += ';secure';
	  if (httpOnly)
	  	c += ';HttpOnly';

	  $doc.cookie = c;
	}-*/;
}
