package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BlockQuoteParser extends BasicMarkdownElementParser {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.BLOCK_QUOTE_REGEX, Pattern.DOTALL);;
	boolean inBlockQuote;
	
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
	public boolean isInMarkdownElement() {
		return inBlockQuote;
	}
	
	@Override
	public boolean isInputSingleLine() {
		return false;
	}
}
