package org.sagebionetworks.web.server.servlet.filter;

public class DreamFilter extends RedirectFilter {

	@Override
	protected String getTargetPage() {
		return "#!Challenges:DREAM";
	}

	@Override
	protected String getUrlPath() {
		return "dream";
	}
}
