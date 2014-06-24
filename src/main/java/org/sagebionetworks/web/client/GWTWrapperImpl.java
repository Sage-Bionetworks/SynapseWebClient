package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.xhr.client.XMLHttpRequest;

public class GWTWrapperImpl implements GWTWrapper {

	@Override
	public String getHostPageBaseURL() {
		return GWT.getHostPageBaseURL();
	}

	@Override
	public String getModuleBaseURL() {
		return GWT.getModuleBaseURL();
	}
	
	@Override
	public void assignThisWindowWith(String url){
		 Window.Location.assign(url);
	}
	
	@Override
	public String encodeQueryString(String queryString){
		return URL.encodeQueryString(queryString);
	}
	
	@Override
	public XMLHttpRequest createXMLHttpRequest() {
		return XMLHttpRequest.create();
	}
	
	@Override
	public NumberFormat getNumberFormat(String pattern) {
		return NumberFormat.getFormat(pattern);
	}

	@Override
	public String getHostPrefix() {
		return com.google.gwt.user.client.Window.Location.getProtocol()+"//"+com.google.gwt.user.client.Window.Location.getHost();
	}
	
	@Override
	public String getCurrentURL() {
		return Window.Location.getHref();
	}
	
	@Override
	public DateTimeFormat getDateTimeFormat() {
		return DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	}
	
	@Override
	public void scheduleExecution(final Callback callback, int delayMillis) {
		Timer timer = new Timer() { 
		    public void run() { 
		    	callback.invoke();
		    } 
		};
		timer.schedule(delayMillis);
	}
	
	
}
