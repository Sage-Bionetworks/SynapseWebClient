package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookmarkTargetParser extends BasicMarkdownElementParser {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.BOOKMARK_TARGET_REGEX);

	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p1.matcher(line.getMarkdown());
		line.updateMarkdown(m.replaceAll("<a id=\"$1\"></a>"));
	}
}
