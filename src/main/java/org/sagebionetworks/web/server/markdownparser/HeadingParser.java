package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HeadingParser extends BasicMarkdownElementParser  {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.HEADING_REGEX, Pattern.DOTALL);;
	boolean matchedLine;
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p1.matcher(line.getMarkdown());
		matchedLine = m.matches();
		if (matchedLine) {
			//looks like a heading
			String prefix = m.group(1);
			String hashes = m.group(2);
            String headingText = m.group(3);
            int level = hashes.length();
            String tag = "h" + level;
            line.updateMarkdown(prefix + "<" + tag + ">" + headingText + "</" + tag + ">");
		}
	}
	
	@Override
	public boolean isBlockElement() {
		return true;
	}
	@Override
	public boolean isInMarkdownElement() {
		return matchedLine;
	}
}
