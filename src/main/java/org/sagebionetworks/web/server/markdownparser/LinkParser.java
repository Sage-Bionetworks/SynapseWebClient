package org.sagebionetworks.web.server.markdownparser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dev.util.collect.HashMap;

public class LinkParser extends BasicMarkdownElementParser  {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.LINK_REGEX, Pattern.DOTALL);
	Pattern protocol = Pattern.compile(MarkdownRegExConstants.LINK_URL_PROTOCOL, Pattern.DOTALL);

	MarkdownExtractor extractor;

	@Override
	public void reset() {
		extractor = new MarkdownExtractor();
	}
	
	private String getCurrentDivID() {
		return WebConstants.DIV_ID_LINK_PREFIX + extractor.getCurrentContainerId() + SharedMarkdownUtils.getPreviewSuffix(isPreview);
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
			StringBuilder updated = new StringBuilder();
			
			//If the "url" targets a bookmarked element in the page, replace it with widget syntax 
			//for the renderer to attach a handler
			if(url.contains(bookmarkTarget)) {
				updated.append(WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.BOOKMARK_CONTENT_TYPE + "?");
				updated.append(WidgetConstants.TEXT_KEY + "=" + text + "&");
				updated.append(WidgetConstants.INLINE_WIDGET_KEY + "=true&");
				updated.append(WidgetConstants.BOOKMARK_KEY + "=" +	url.substring(bookmarkTarget.length()));
				updated.append(WidgetConstants.WIDGET_END_MARKDOWN);			
			} else {
				//Check for incomplete urls (i.e. urls starting without http/ftp/file/#)
				String testUrl = url.toLowerCase();
				Matcher protocolMatcher = protocol.matcher(testUrl);
				if(!protocolMatcher.find() && !testUrl.startsWith("#")) {
					url = WebConstants.URL_PROTOCOL + url;
				}
				
				updated.append(extractor.getContainerElementStart() + getCurrentDivID());
				updated.append("\">" + extractor.getContainerElementEnd());
				
				StringBuilder html = new StringBuilder();
				html.append(ServerMarkdownUtils.START_LINK);
				html.append(url + "\">");
				html.append(text + ServerMarkdownUtils.END_LINK);
				extractor.putContainerIdToContent(getCurrentDivID(), html.toString());
			}
			//Escape the replacement string for bookmarks' widget syntax
			m.appendReplacement(sb, Matcher.quoteReplacement(updated.toString()));
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	
	@Override
	public void completeParse(Document doc) {
		for(String key: extractor.getContainerIds()) {
			Element el = doc.getElementById(key);
			if(el != null) {
				el.prepend(extractor.getContent(key));
				
			}
		}
	}
	
}
