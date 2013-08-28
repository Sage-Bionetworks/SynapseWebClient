package org.sagebionetworks.web.server.markdownparser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StrikeoutParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.STRIKE_OUT_REGEX);
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		line.updateMarkdown(m.replaceAll("<del>$1</del>"));
	}
}
