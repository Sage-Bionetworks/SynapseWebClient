package org.sagebionetworks.web.server.markdownparser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dev.util.collect.HashMap;

public class UrlAutoLinkParser extends BasicMarkdownElementParser {
	Pattern p = Pattern.compile(MarkdownRegExConstants.LINK_URL);
	Map<String, String> div2Autolink = new HashMap<String, String>();
	int autolinkCount;

	@Override
	public void reset() {
		autolinkCount = -1;
		div2Autolink.clear();
	}
	
	private String getCurrentDivID() {
		return WebConstants.DIV_ID_AUTOLINK_PREFIX + autolinkCount;
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			autolinkCount++;
			String url = m.group(1).trim();
			String updated = "<div class=\"inline-block\" id=\"" + getCurrentDivID() + "\"></div>";
			StringBuilder html = new StringBuilder();
			html.append("<a class=\"link\" target=\"_blank\" href=\"");
			html.append(url + "\">");
			html.append(url + "</a>");
			div2Autolink.put(getCurrentDivID(), html.toString());
			m.appendReplacement(sb, updated);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	/*
	@Override
	public void completeParse(Document doc) {
		for(String key: div2Autolink.keySet()) {
			Element el = doc.getElementById(key);
			el.appendText(div2Autolink.get(key));
		}
	}
	*/
}
