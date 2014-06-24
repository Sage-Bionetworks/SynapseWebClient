package org.sagebionetworks.web.client;

import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.xhr.client.XMLHttpRequest;

public interface GWTWrapper {
	String getHostPageBaseURL();

	String getModuleBaseURL();

	void assignThisWindowWith(String url);

	String encodeQueryString(String queryString);

	XMLHttpRequest createXMLHttpRequest();

	NumberFormat getNumberFormat(String pattern);
	
	String getHostPrefix();
	
	String getCurrentURL();
	
	DateTimeFormat getDateTimeFormat();
	
	void scheduleExecution(Callback callback, int delay);
}
