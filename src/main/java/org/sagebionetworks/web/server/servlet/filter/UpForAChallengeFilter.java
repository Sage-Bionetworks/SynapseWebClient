package org.sagebionetworks.web.server.servlet.filter;

public class UpForAChallengeFilter extends RedirectFilter {
	@Override
	protected String getTargetPage() {
		return "#!Synapse:syn3157598";
	}
	
	@Override
	protected String getUrlPath() {
		return "upforachallenge";
	}
}
