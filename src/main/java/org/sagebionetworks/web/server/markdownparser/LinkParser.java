package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.shared.WebConstants;

public class LinkParser extends BasicMarkdownElementParser  {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.LINK_REGEX, Pattern.DOTALL);
	Pattern protocol = Pattern.compile(MarkdownRegExConstants.LINK_URL_PROTOCOL, Pattern.DOTALL);

	@Override
	public void processLine(MarkdownElements line) {
		String input = line.getMarkdown();
		String bookmarkTarget = WidgetConstants.BOOKMARK_LINK_IDENTIFIER + ":";
		Matcher m = p1.matcher(input);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			String text = input.substring(m.start(2), m.end(2));
			String url = input.substring(m.start(3), m.end(3));
			String updated;
			
			//If the "url" targets a bookmarked element in the page, replace it with widget syntax 
			//for the renderer to attach a handler
			if(url.contains(bookmarkTarget)) {
				updated = WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.BOOKMARK_CONTENT_TYPE + "?" + 
				WidgetConstants.TEXT_KEY + "=" + text + "&" + WidgetConstants.INLINE_WIDGET_KEY + "=true&" + WidgetConstants.BOOKMARK_KEY + "=" +
				url.substring(bookmarkTarget.length()) + WidgetConstants.WIDGET_END_MARKDOWN;			

			} else {
				//Check for incomplete urls (i.e. urls starting without http/ftp/file/#)
				String testUrl = url.toLowerCase();
				Matcher protocolMatcher = protocol.matcher(testUrl);
				if(!protocolMatcher.find() && !testUrl.startsWith("#")) {
					url = WebConstants.URL_PROTOCOL + url;
				}
				
				//Create link by preparing widget syntax for the renderer
				String encodedUrl = WidgetEncodingUtil.encodeValue(url);
				
				//${link?text=text&url=url&inlineWidget=true}
				updated = WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.LINK_CONTENT_TYPE + "?" + 
				WidgetConstants.TEXT_KEY + "=" + text + "&" + WidgetConstants.LINK_URL_KEY + "=" + encodedUrl + "&" + 
				WidgetConstants.INLINE_WIDGET_KEY + "=true" + WidgetConstants.WIDGET_END_MARKDOWN;
			}
			
			//Escape the replacement string for appendReplacement
			updated = Matcher.quoteReplacement(updated);
			m.appendReplacement(sb, updated);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
}
