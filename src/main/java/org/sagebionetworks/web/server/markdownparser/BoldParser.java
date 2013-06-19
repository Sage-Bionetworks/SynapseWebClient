package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class BoldParser implements MarkdownElementParser {
	Pattern p1;
	public static final String BOLD_REGEX = "(\\*\\*|__)(?=\\S)(.+?[*_]*)(?<=\\S)\\1";
	@Override
	public void init() {
		p1 = Pattern.compile(BOLD_REGEX);
	}

	@Override
	public void reset() {
		//no state
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		return m.replaceAll("<strong>$2</strong>");
	}
	
	@Override
	public void completeParse(StringBuilder html) {
	}
	
	@Override
	public boolean isInMarkdownElement() {
		return false;
	}
}
