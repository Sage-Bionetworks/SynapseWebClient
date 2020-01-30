package org.sagebionetworks.web.server.servlet.filter;

public class RegisterAccountFilter extends RedirectFilter {

	public static final String URL_PATH = "register";

	@Override
	protected String getTargetPage() {
		return "#!RegisterAccount:0";
	}

	@Override
	protected String getUrlPath() {
		return URL_PATH;
	}
}
