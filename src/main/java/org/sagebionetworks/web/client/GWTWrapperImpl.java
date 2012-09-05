package org.sagebionetworks.web.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

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
	public void replaceThisWindowWith(String url){
		 Window.Location.replace(url);
	}
	
	@Override
	public String encodeQueryString(String queryString){
		return URL.encodeQueryString(queryString);
	}

}
