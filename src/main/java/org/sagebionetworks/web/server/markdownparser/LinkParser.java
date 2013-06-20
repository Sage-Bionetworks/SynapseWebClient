package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class LinkParser extends BasicMarkdownElementParser  {
	Pattern p1;
	@Override
	public void init() {
		p1 = Pattern.compile(MarkdownRegExConstants.LINK_REGEX, Pattern.DOTALL);
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		return m.replaceAll("<a href=\"$3\">$2</a>");
	}
}
