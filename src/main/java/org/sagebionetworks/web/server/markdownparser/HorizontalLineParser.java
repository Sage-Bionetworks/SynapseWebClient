package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Pattern;


public class HorizontalLineParser extends BasicMarkdownElementParser  {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.HR_REGEX1);
	Pattern p2 = Pattern.compile(MarkdownRegExConstants.HR_REGEX2);

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
}
