package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class CodeParser extends BasicMarkdownElementParser  {
	Pattern p = Pattern.compile(MarkdownRegExConstants.FENCE_CODE_BLOCK_REGEX);
	boolean isInCodeBlock, isFirstCodeLine;
	
	
	@Override
	public void reset() {
		isInCodeBlock = false;
		isFirstCodeLine = false;
	}

	@Override
	public void processLine(MarkdownElements line) {
		Matcher m = p.matcher(line.getMarkdown());
		if (m.matches()) {
			if (!isInCodeBlock) {
				//starting code block
				isInCodeBlock = true;
				isFirstCodeLine = true;
				StringBuilder sb = new StringBuilder();
				sb.append(ServerMarkdownUtils.START_PRE_CODE);
				String codeCssClass = null;
				if (m.groupCount() == 2)
					codeCssClass = m.group(2).toLowerCase();
				if (codeCssClass == null || codeCssClass.trim().length() == 0) {
					codeCssClass = ServerMarkdownUtils.DEFAULT_CODE_CSS_CLASS;
				}
				sb.append(" class=\""+codeCssClass+"\"");
				sb.append(">");
				line.prependElement(sb.toString());
			}
			else {
				//ending code block
				line.appendElement(ServerMarkdownUtils.END_PRE_CODE);
				isInCodeBlock = false;
			}
			//remove all fenced code blocks from the markdown, just set to the prefix group
			line.updateMarkdown(m.group(1));
		}
		else {
			if (isInCodeBlock && !isFirstCodeLine)
				line.prependElement("\n");
			
			if (isFirstCodeLine)
				isFirstCodeLine = false;
		}
	}

	@Override
	public boolean isInMarkdownElement() {
		return isInCodeBlock;
	}
	
	@Override
	public boolean isBlockElement() {
		return true;
	}
	
	@Override
	public boolean isInputSingleLine() {
		return false;
	}

}
