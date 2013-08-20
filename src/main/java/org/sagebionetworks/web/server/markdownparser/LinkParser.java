package org.sagebionetworks.web.server.markdownparser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dev.util.collect.HashMap;

public class LinkParser extends BasicMarkdownElementParser  {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.LINK_REGEX, Pattern.DOTALL);
	Pattern protocol = Pattern.compile(MarkdownRegExConstants.LINK_URL_PROTOCOL, Pattern.DOTALL);
	Map<String, String> div2Link = new HashMap<String, String>();
	int linkCount;

	@Override
	public void reset() {
		linkCount = -1;
		div2Link.clear();
	}
	
	private String getCurrentDivID() {
		return WebConstants.DIV_ID_LINK_PREFIX + linkCount;
	}

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
				linkCount++;
				//Check for incomplete urls (i.e. urls starting without http/ftp/file/#)
				String testUrl = url.toLowerCase();
				Matcher protocolMatcher = protocol.matcher(testUrl);
				if(!protocolMatcher.find() && !testUrl.startsWith("#")) {
					url = WebConstants.URL_PROTOCOL + url;
				}
				
				updated = "<div class=\"inline-block\" id=\"" + getCurrentDivID() + "\"></div>";
				StringBuilder html = new StringBuilder();
				html.append("<a class=\"link\" target=\"_blank\" href=\"");
				html.append(url + "\">");
				html.append(text + "</a>");
				div2Link.put(getCurrentDivID(), html.toString());

			}
			m.appendReplacement(sb, updated);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	/*
	@Override
	public void completeParse(Document doc) {
		for(String key: div2Link.keySet()) {
			Element el = doc.getElementById(key);
			el.appendText(div2Link.get(key));
		}
	}
	*/
}
