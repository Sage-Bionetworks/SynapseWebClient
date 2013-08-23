package org.sagebionetworks.web.server.markdownparser;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItalicsParser extends BasicMarkdownElementParser  {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.ITALICS_REGEX);;
	
	@Override
	public void processLine(MarkdownElements line, List<MarkdownElementParser> simpleParsers) {
		Matcher m = p1.matcher(line.getMarkdown());
		line.updateMarkdown(m.replaceAll("<em>$2</em>"));
	}
}
