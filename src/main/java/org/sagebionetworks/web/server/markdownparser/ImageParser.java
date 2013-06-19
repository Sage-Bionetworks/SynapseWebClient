package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class ImageParser extends BasicMarkdownElementParser {
	Pattern p1;
	public static final String IMAGE_REGEX = "!\\[(.*)\\]\\((.*)\\)";
	@Override
	public void init() {
		p1 = Pattern.compile(IMAGE_REGEX);
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		return m.replaceAll("<img src=\"$2\" alt=\"$1\" />");
	}
}
