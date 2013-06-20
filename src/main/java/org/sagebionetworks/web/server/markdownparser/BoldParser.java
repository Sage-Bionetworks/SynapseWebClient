package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoldParser extends BasicMarkdownElementParser  {
	Pattern p1;
	@Override
	public void init() {
		p1 = Pattern.compile(MarkdownRegExConstants.BOLD_REGEX);
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		return m.replaceAll("<strong>$2</strong>");
	}
}
