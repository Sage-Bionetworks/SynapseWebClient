package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class BlockQuoteParser implements MarkdownElementParser {
	Pattern p1;
	boolean inBlockQuote;
	public static final String BLOCK_QUOTE_REGEX = "(^\\s*>\\s?(.+))";
	@Override
	public void init() {
		p1 = Pattern.compile(BLOCK_QUOTE_REGEX, Pattern.DOTALL);
	}

	@Override
	public void reset() {
		inBlockQuote = false;
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		StringBuilder output = new StringBuilder();
		if (m.matches()) {
			if (!inBlockQuote) {
				//starting block quote
				inBlockQuote = true;
				output.append("<blockquote>");
			} 
			output.append(m.group(2));
		}
		else {
			if (inBlockQuote){
				inBlockQuote = false;
				//finish block quote
				output.append("</blockquote>");
			}
			output.append(line);
		}
		return output.toString();
	}
	
	@Override
	public void completeParse(StringBuilder html) {
	}

	@Override
	public boolean isInMarkdownElement() {
		return inBlockQuote;
	}
}
