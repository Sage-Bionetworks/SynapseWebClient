package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class CodeSpanParser extends BasicMarkdownElementParser {
	Pattern p1;
	public static final String CODE_SPAN_REGEX = "(?<!\\\\)(`+)(.+?)(?<!`)\\1(?!`)";
	@Override
	public void init() {
		p1 = Pattern.compile(CODE_SPAN_REGEX);
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		return m.replaceAll("<code>$2</code>");
	}
}
