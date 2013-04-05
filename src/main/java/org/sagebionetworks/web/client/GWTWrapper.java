package org.sagebionetworks.web.client;

import com.google.gwt.xhr.client.XMLHttpRequest;

public interface GWTWrapper {
	String getHostPageBaseURL();
	String getModuleBaseURL();
	void replaceThisWindowWith(String url);
	String encodeQueryString(String queryString);
	XMLHttpRequest createXMLHttpRequest();
}
