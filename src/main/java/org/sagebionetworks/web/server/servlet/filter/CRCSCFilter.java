package org.sagebionetworks.web.server.servlet.filter;

public class CRCSCFilter extends RedirectFilter {
	@Override
	protected String getTargetPage() {
		return "#!Synapse:syn2623706";
	}
	
	@Override
	protected String getUrlPath() {
		return "crcsc";
	}
}
