package org.sagebionetworks.web.server.markdownparser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dev.util.collect.HashMap;

public class ImageParser extends BasicMarkdownElementParser {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.IMAGE_REGEX);;
	Map<String, String> div2Image = new HashMap<String, String>();
	int imageCount;

	@Override
	public void reset() {
		imageCount = -1;
		div2Image.clear();
	}
	
	private String getCurrentDivID() {
		return WebConstants.DIV_ID_IMAGE_PREFIX + imageCount;
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p1.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			imageCount++;
			String updated = "<div class=\"inline-block\" id=\"" + getCurrentDivID() + "\"></div>";
			
			String src = m.group(2);
			String alt = m.group(1);
			StringBuilder html = new StringBuilder();
			html.append("<img src=\"");
			html.append(src + "\" alt=\"");
			html.append(alt + "\" />");
			div2Image.put(getCurrentDivID(), html.toString());
			m.appendReplacement(sb, updated);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	/*
	@Override
	public void completeParse(Document doc) {
		for(String key: div2Image.keySet()) {
			Element el = doc.getElementById(key);
			el.appendText(div2Image.get(key));
		}
	}
	*/
}
