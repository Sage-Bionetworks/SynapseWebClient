package org.sagebionetworks.web.server.markdownparser;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookmarkTargetParser extends BasicMarkdownElementParser {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.BOOKMARK_TARGET_REGEX);

	@Override
	public void processLine(MarkdownElements line, List<MarkdownElementParser> simpleParsers) {
		Matcher m = p1.matcher(line.getMarkdown());
		//Assign the inline style so bookmarks can be placed generally anywhere
		line.updateMarkdown(m.replaceAll("<p class=\"inlineWidgetContainer\" id=\"$1\"></p>"));
	}
}
