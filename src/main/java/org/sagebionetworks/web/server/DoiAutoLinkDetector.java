package org.sagebionetworks.web.server;

import java.util.regex.Matcher;

public class DoiAutoLinkDetector extends AutoLinkDetector {

	@Override
	public String getRegularExpression() {
		return "\\W*(doi:([a-zA-Z_0-9./]+))\\W*";
	}

	@Override
	public int getCorrectGroupCount() {
		return 2;
	}

	@Override
	public String getLinkHtml(Matcher matcher) {
		return ServerMarkdownUtils.getDoiLink(matcher.group(1), matcher.group(2));
	}

}
