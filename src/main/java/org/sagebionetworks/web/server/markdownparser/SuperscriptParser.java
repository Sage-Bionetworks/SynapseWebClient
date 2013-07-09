package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SuperscriptParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.SUPERSCRIPT_REGEX);
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		line.updateMarkdown(m.replaceAll("<sup>$2</sup>"));
	}
}
