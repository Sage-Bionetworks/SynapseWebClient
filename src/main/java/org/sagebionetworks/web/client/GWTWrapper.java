package org.sagebionetworks.web.client;

public interface GWTWrapper {
	String getHostPageBaseURL();
	String getModuleBaseURL();
	void replaceThisWindowWith(String url);
	String encodeQueryString(String queryString);
}
