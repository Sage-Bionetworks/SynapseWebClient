package org.sagebionetworks.web.server.markdownparser;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class BlockQuoteParser extends BasicMarkdownElementParser {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.BLOCK_QUOTE_REGEX, Pattern.DOTALL);;
	Pattern p2 = Pattern.compile(MarkdownRegExConstants.FENCE_CODE_BLOCK_REGEX, Pattern.DOTALL);
	boolean inBlockQuote;
	
	@Override
	public void reset(List<MarkdownElementParser> simpleParsers) {
		inBlockQuote = false;
	}

	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p1.matcher(line.getMarkdown());
		Matcher codeMatcher = p2.matcher(line.getMarkdown());
		if (m.matches()) {
			if (!inBlockQuote) {
				//starting block quote
				inBlockQuote = true;
				line.prependElement(ServerMarkdownUtils.START_BLOCKQUOTE_TAG);
			}
			//modify the markdown and preserve leading space to determine depth of list items
			//do not preserve any space following ">" if this is a code block fence
			StringBuilder sb = new StringBuilder();
			if(!codeMatcher.matches()) {
				sb.append(m.group(3));
			}
			sb.append(m.group(4));
			line.updateMarkdown(sb.toString());
		}
		else {
			if (inBlockQuote){
				inBlockQuote = false;
				//finish block quote
				line.prependElement(ServerMarkdownUtils.END_BLOCKQUOTE_TAG);
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
