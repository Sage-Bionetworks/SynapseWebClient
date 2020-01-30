package org.sagebionetworks.web.server.servlet.filter;

public class DigitalHealthFilter extends RedirectFilter {

	@Override
	protected String getTargetPage() {
		return "#!StandaloneWiki:DigitalHealth";
	}

	@Override
	protected String getUrlPath() {
		return "digitalhealth";
	}
}
