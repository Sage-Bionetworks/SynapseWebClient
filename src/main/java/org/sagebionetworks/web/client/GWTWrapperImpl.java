package org.sagebionetworks.web.client;

import com.google.gwt.core.client.GWT;

public class GWTWrapperImpl implements GWTWrapper {

	@Override
	public String getHostPageBaseURL() {
		return GWT.getHostPageBaseURL();
	}

	@Override
	public String getModuleBaseURL() {
		return GWT.getModuleBaseURL();
	}

}
