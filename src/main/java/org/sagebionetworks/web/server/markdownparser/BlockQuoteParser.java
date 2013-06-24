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
	public void processLine(MarkdownElements line) {
		Matcher m = p1.matcher(line.getMarkdown());
		
		if (m.matches()) {
			if (!inBlockQuote) {
				//starting block quote
				inBlockQuote = true;
				line.prependElement("<blockquote>");
			}
			//modify the markdown
			line.updateMarkdown(m.group(2));
		}
		else {
			if (inBlockQuote){
				inBlockQuote = false;
				//finish block quote
				line.prependElement("</blockquote>");
			}
			//no need to modify the markdown
		}
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
