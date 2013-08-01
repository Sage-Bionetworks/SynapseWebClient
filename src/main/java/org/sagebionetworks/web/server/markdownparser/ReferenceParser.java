package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;

import com.google.gwt.dev.util.collect.HashMap;

public class ReferenceParser extends BasicMarkdownElementParser {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.REFERENCE_REGEX);
	ArrayList<String> footnotes;
	HashMap<Integer, String> urlMap;
	int footnoteNumber;
	
	@Override
	public void reset() {
		footnotes = new ArrayList<String>();
		urlMap = new HashMap<Integer, String>();
		footnoteNumber = 1;
	}

	@Override
	public void processLine(MarkdownElements line) {
		String input = line.getMarkdown();
		Matcher m = p1.matcher(input);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//Expression has 6 groupings (3 parameter/value pairs.)
			//Store the reference text, and the url, mapped to the appropriate footnoteId
			for(int i = 1; i < 6; i += 2) {
				String param = input.substring(m.start(i), m.end(i));
				if(param.contains("text")) {
					footnotes.add(input.substring(m.start(i + 1), m.end(i + 1)));
				}
				if(param.contains("url")) {
					urlMap.put(footnoteNumber, input.substring(m.start(i + 1), m.end(i + 1)));	
				}
			}
			
			/*
			 * Insert:
			 * 1) Bookmark target so that footnotes can link back to the reference
			 * 2) add a footnoteId param to the original syntax to tell the renderer which footnote to link to
			 */
			String referenceId = WebConstants.REFERENCE_ID_WIDGET_PREFIX + footnoteNumber;
			String footnoteParameter = WidgetConstants.REFERENCE_FOOTNOTE_KEY + "=" + footnoteNumber;
			
			String updated = "<p class=\"inlineWidgetContainer\" id=\"" + referenceId + "\"></p>" + input.substring(m.start(), m.end() - 1) + "&" + footnoteParameter + "}";
			updated = Matcher.quoteReplacement(updated);	//Escapes the replacement string for appendReplacement
			m.appendReplacement(sb, updated);
			footnoteNumber++;
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		html.append("<hr>");
		
		for(int i = 0; i < footnotes.size(); i++) {
			String footnoteText = WidgetEncodingUtil.decodeValue(footnotes.get(i));
			String targetReferenceId = WebConstants.REFERENCE_ID_WIDGET_PREFIX + (i + 1);
			String footnoteId = WebConstants.FOOTNOTE_ID_WIDGET_PREFIX + (i + 1);
			
			StringBuilder sb = new StringBuilder();
			boolean hasUrl = !urlMap.get(i + 1).equals("false");
			
			//Insert the special bookmark-link syntax to link back to the reference
			sb.append("[[" + (i + 1) + "]](" + WidgetConstants.BOOKMARK_LINK_IDENTIFIER + ":" + targetReferenceId + ")");

			//Assign id to the element so that the reference can link to this footnote
			sb.append("<p id=\"" + footnoteId + "\" class=\"inlineWidgetContainer\">");
			
			//If this footnote needs to be a hyperlink, add the link syntax
			if(hasUrl) {
				String url = WidgetEncodingUtil.decodeValue(urlMap.get(i + 1));
				sb.append("</p>[" + footnoteText + "](" + url + ")");
			} else {
				sb.append(footnoteText + "</p>");
			}
			sb.append("<br>");
			html.append(sb.toString());
		}
	}
}
