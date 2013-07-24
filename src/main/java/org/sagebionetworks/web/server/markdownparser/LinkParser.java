package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

public class LinkParser extends BasicMarkdownElementParser  {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.LINK_REGEX, Pattern.DOTALL);

	@Override
	public void processLine(MarkdownElements line) {
		String input = line.getMarkdown();
		String bookmarkTarget = "#Bookmark:";
		Matcher m = p1.matcher(input);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String text = input.substring(m.start(2), m.end(2));
			String url = input.substring(m.start(3), m.end(3));
			String updated;
			
			//If the "url" targets a bookmarked element in the page, replace it with widget syntax 
			//for the renderer to attach a handler
			if(url.contains(bookmarkTarget)) {
				updated = "${" + WidgetConstants.BOOKMARK_CONTENT_TYPE + "?text=" + text + "&" + WidgetConstants.INLINE_WIDGET_KEY + "=true&" +
								WidgetConstants.BOOKMARK_KEY + "=" + url.substring(bookmarkTarget.length()) + "}";		
			} else {
				updated = "<a class=\"link\" target=\"_blank\" href=\"" + url + "\">" + text + "</a>";
			}
			
			//Escape the replacement string for appendReplacement
			updated = Matcher.quoteReplacement(updated);
			m.appendReplacement(sb, updated);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
}
