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
			//Store the reference text, specified by the text parameter
			String firstParam = input.substring(m.start(1), m.end(1));
			if(firstParam.contains("text=")) {
				footnotes.add(input.substring(m.start(2), m.end(2)));
			} else {
				footnotes.add(input.substring(m.start(4), m.end(4)));
			}
			
			/*
			 * Insert any extra parameters/values by appending to the widget's original expression/parameters
			 * (Don't forget the closing "}") Use as a replacement string:
			 * &footnoteId=# (this id tells the renderer which element to link to)
			*/
			String updated = input.substring(m.start(), m.end() - 1) + "&" + WidgetConstants.REFERENCE_FOOTNOTE_KEY + "=" + footnoteNumber + "}";
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
