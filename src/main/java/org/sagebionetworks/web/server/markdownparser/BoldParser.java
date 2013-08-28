package org.sagebionetworks.web.server.markdownparser;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BoldParser extends BasicMarkdownElementParser  {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.BOLD_REGEX);
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p1.matcher(line.getMarkdown());
		line.updateMarkdown(m.replaceAll("<strong>$2</strong>"));
	}
}
