package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;

public class ReferenceParser extends BasicMarkdownElementParser {
	Pattern p1= Pattern.compile(MarkdownRegExConstants.REFERENCE_REGEX);
	ArrayList<String> footnotes;
	int footnoteNumber;
	
	@Override
	public void reset() {
		footnotes = new ArrayList<String>();
		footnoteNumber = 1;
	}

	@Override
	public void processLine(MarkdownElements line) {
		String input = line.getMarkdown();
		Matcher m = p1.matcher(input);
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//Store reference text and insert extra parameter for the renderer to link to
			int lenOfSyntax = "${reference?text=".length();
			footnotes.add(input.substring(m.start() + lenOfSyntax, m.end() - 1));
			String updated = input.substring(m.start(), m.end() - 1) + 
				"&" + WidgetConstants.INLINE_WIDGET_KEY + "=true&" + WidgetConstants.REFERENCE_FOOTNOTE_KEY + "=" + footnoteNumber + "}";
			updated = Matcher.quoteReplacement(updated);	//Escapes the replacement string for appendReplacement
			m.appendReplacement(sb, updated);
			footnoteNumber++;
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		for(int i = 0; i < footnotes.size(); i++) {
			String text = WidgetEncodingUtil.decodeValue(footnotes.get(i));
			String footnote = "<p id=\"" + WebConstants.FOOTNOTE_ID_WIDGET_PREFIX + (i + 1) + "\">[" + (i + 1) + "] " + text + "</p>";
			html.append(footnote);
		}
	}
}
