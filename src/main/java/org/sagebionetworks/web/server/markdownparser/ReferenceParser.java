package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.ArrayList;

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
			footnotes.add(input.substring(m.start() + 17, m.end() - 1));
			String updated = input.substring(m.start(), m.end() - 1) + "&footnoteId=" + footnoteNumber + "}";
			updated = updated.replaceAll("\\$", "\\\\\\$");	//Escape for the appendReplacement method
			m.appendReplacement(sb, updated);
			footnoteNumber++;
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		for(int i = 0; i < footnotes.size(); i++) {
			String text = footnotes.get(i).replaceAll("%2E", ".");
			String footnote = "<p id=\"footnote" + (i + 1) + "\">[" + (i + 1) + "] " + text + "</p>";
			html.append(footnote);
		}
	}
}
