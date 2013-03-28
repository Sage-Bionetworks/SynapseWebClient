package org.sagebionetworks.web.server;

import java.util.regex.Matcher;

public class SynapseAutoLinkDetector extends AutoLinkDetector {

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
