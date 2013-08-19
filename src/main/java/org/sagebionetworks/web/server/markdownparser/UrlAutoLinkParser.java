package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;

public class UrlAutoLinkParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.LINK_URL);

	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//Create link by preparing widget syntax for the renderer
			String encodedUrl = WidgetEncodingUtil.encodeValue(m.group(1).trim());
			
			//${link?text=url&url=url&inlineWidget=true}
			String updated = WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.LINK_CONTENT_TYPE + "?" + 
			WidgetConstants.TEXT_KEY + "=" + encodedUrl + "&" + WidgetConstants.LINK_URL_KEY + "=" + encodedUrl + "&" + 
			WidgetConstants.INLINE_WIDGET_KEY + "=true" + WidgetConstants.WIDGET_END_MARKDOWN;
			
			//Escape the replacement string for appendReplacement
			updated = Matcher.quoteReplacement(updated);
			m.appendReplacement(sb, updated);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
}
