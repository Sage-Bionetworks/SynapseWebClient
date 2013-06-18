package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * One of the more complicated parsers.  Needs to have a stack to support nested lists.
 * @author jayhodgson
 *
 */
public class ListParser implements MarkdownElementParser {
	Pattern p1;
	public static final String HEADING_REGEX = "^(#{1,6})\\s*(.*)$";
	@Override
	public void init() {
		p1 = Pattern.compile(HEADING_REGEX);
	}

	@Override
	public void reset() {
		//no state
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		if (m.matches()) {
			//looks like a heading
			String hashes = m.group(1);
            String headingText = m.group(2);
            int level = hashes.length();
            String tag = "h" + level;
            return "<" + tag + ">" + headingText + "</" + tag + ">";
		}
		return line;
	}
	
	@Override
	public void completeParse(StringBuilder html) {
	}

}
