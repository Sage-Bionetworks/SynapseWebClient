package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class BacktickParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.BACKTICK_ESCAPED_REGEX);
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		String backtickWidgetSyntax = WidgetConstants.WIDGET_START_MARKDOWN_ESCAPED + WidgetConstants.BACKTICK_CONTENT_TYPE + "?" + WidgetConstants.TEXT_KEY + "=%60&" + WidgetConstants.INLINE_WIDGET_KEY + "=true" + WidgetConstants.WIDGET_END_MARKDOWN_ESCAPED;
		line.updateMarkdown(m.replaceAll(backtickWidgetSyntax));
	}

}
