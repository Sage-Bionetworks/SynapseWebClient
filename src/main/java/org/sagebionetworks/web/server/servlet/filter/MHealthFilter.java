package org.sagebionetworks.web.server.servlet.filter;

public class MHealthFilter extends RedirectFilter {

	@Override
	protected String getTargetPage() {
		return "#!StandaloneWiki:DigitalHealth";
	}

	@Override
	protected String getUrlPath() {
		return "mHealth";
	}
}
