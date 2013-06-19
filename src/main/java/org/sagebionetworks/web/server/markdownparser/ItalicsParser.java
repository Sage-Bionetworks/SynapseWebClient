package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class ItalicsParser implements MarkdownElementParser {
	Pattern p1;
	public static final String ITALICS_REGEX = "(\\*|_)(?=\\S)(.+?)(?<=\\S)\\1";
	@Override
	public void init() {
		p1 = Pattern.compile(ITALICS_REGEX);
	}

	@Override
	public void reset() {
		//no state
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		return m.replaceAll("<em>$2</em>");
	}
	
	@Override
	public void completeParse(StringBuilder html) {
	}

	@Override
	public boolean isInMarkdownElement() {
		return false;
	}
}
