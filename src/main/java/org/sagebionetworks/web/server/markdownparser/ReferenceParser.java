package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;

public class ReferenceParser extends BasicMarkdownElementParser {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.REFERENCE_REGEX);
	ArrayList<String> footnotes;
	List<MarkdownElementParser> parsersOnCompletion;
	int footnoteNumber;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		footnotes = new ArrayList<String>();
		footnoteNumber = 1;
		parsersOnCompletion = simpleParsers;
	}

	@Override
	public void processLine(MarkdownElements line) {
		String input = line.getMarkdown();
		Matcher m = p1.matcher(input);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//Expression has 4 groupings (2 parameter/value pairs.)
			//Store the reference text
			for(int i = 1; i < 4; i += 2) {
				String param = input.substring(m.start(i), m.end(i));
				if(param.contains("text")) {
					footnotes.add(input.substring(m.start(i + 1), m.end(i + 1)));
				}
			}
			
			/*
			 * Insert:
			 * 1) Bookmark target so that footnotes can link back to the reference
			 * 2) add a footnoteId param to the original syntax to tell the renderer which footnote to link to
			 */
			String referenceId = WebConstants.REFERENCE_ID_WIDGET_PREFIX + footnoteNumber;
			String footnoteParameter = WidgetConstants.REFERENCE_FOOTNOTE_KEY + "=" + footnoteNumber;
			
			String updated = "<span id=\"" + referenceId + "\"></span>" + input.substring(m.start(), m.end() - 1) + "&" + footnoteParameter + "}";
			updated = Matcher.quoteReplacement(updated);	//Escapes the replacement string for appendReplacement
			m.appendReplacement(sb, updated);
			footnoteNumber++;
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		if (footnotes.size() > 0)
			html.append("<hr>");
		StringBuilder footnoteMarkdown = new StringBuilder();
		for(int i = 0; i < footnotes.size(); i++) {
			String footnoteText = WidgetEncodingUtil.decodeValue(footnotes.get(i));
			String targetReferenceId = WebConstants.REFERENCE_ID_WIDGET_PREFIX + (i + 1);
			String footnoteId = WebConstants.FOOTNOTE_ID_WIDGET_PREFIX + (i + 1);
			
			//Insert the special bookmark-link syntax to link back to the reference
			footnoteMarkdown.append("[[" + (i + 1) + "]](" + WidgetConstants.BOOKMARK_LINK_IDENTIFIER + ":" + targetReferenceId + ") ");

			//Assign id to the element so that the reference can link to this footnote
			footnoteMarkdown.append("<span id=\"" + footnoteId + "\" class=\"moveup-4\">" + footnoteText + "</span>");
			footnoteMarkdown.append("<br>");
		}
		String parsedFootnotes = runSimpleParsers(footnoteMarkdown.toString(), parsersOnCompletion);
		html.append(parsedFootnotes);
	}
}
