package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class HorizontalLineParser implements MarkdownElementParser {
	Pattern p1, p2;
	
	@Override
	public void init() {
		p1 = Pattern.compile(ServerMarkdownUtils.HR_REGEX1);
		p2 = Pattern.compile(ServerMarkdownUtils.HR_REGEX2);
	}

	@Override
	public void reset() {
		//no state
	}

	@Override
	public String processLine(String line) {
		String testLine = line.replaceAll(" ", "");
		boolean isHr = p1.matcher(testLine).matches() || p2.matcher(testLine).matches();
		if (isHr) {
			//output hr
			return "<hr>";
		}
		return line;
	}
	
	@Override
	public void completeParse(StringBuilder html) {
	}

}
