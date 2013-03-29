package org.sagebionetworks.web.server;

import java.util.regex.Matcher;

public class SynapseAutoLinkDetector extends AutoLinkDetector {
	
	private static SynapseAutoLinkDetector instance;
	protected SynapseAutoLinkDetector(){};
	public static SynapseAutoLinkDetector getInstance() {
		if (instance == null)
			instance = new SynapseAutoLinkDetector();
		return instance;
	}
	@Override
	public String getRegularExpression() {
		return "\\W*(syn\\d+)\\W*";
	}

	@Override
	public int getCorrectGroupCount() {
		return 1;
	}

	@Override
	public String getLinkHtml(Matcher matcher) {
		return ServerMarkdownUtils.getSynAnchorHtml(matcher.group(1));
	}

}
