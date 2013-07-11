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
			//store match and then replace
			footnotes.add(input.substring(m.start() + 2, m.end() - 1));
			m.appendReplacement(sb, "<a target=\"_self\" href=\"#footnote" + footnoteNumber + "\">[" + footnoteNumber + "]</a>");
			footnoteNumber++;
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		for(int i = 0; i < footnotes.size(); i++) {
			String footnote = "<a name=\"footnote" + (i + 1) + "\">[" + (i + 1) + "] " + footnotes.get(i) + "</a><br />";
			html.append(footnote);
		}
	}
}
