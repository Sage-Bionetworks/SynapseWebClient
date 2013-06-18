package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class CodeParser implements MarkdownElementParser {
	Pattern p;
	//exactly three '`', optionally followed by the language class to use
	public static final String FENCE_CODE_BLOCK_REGEX = "^[> \t\n\f\r]*[`]{3}\\s*([a-zA-Z_0-9-]*)\\s*$";
	boolean isInCodeBlock;
	@Override
	public void init() {
		p = Pattern.compile(FENCE_CODE_BLOCK_REGEX);
	}

	@Override
	public void reset() {
		isInCodeBlock = false;
	}

	@Override
	public String processLine(String line) {
		StringBuilder sb = new StringBuilder();
		Matcher m = p.matcher(line);
		if (m.matches()) {
			if (!isInCodeBlock) {
				//starting code block
				isInCodeBlock = true;
				sb.append(ServerMarkdownUtils.START_PRE_CODE);
				if (m.groupCount() == 1)
					sb.append(" class=\""+m.group(1).toLowerCase()+"\"");
				sb.append(">");
			}
			else {
				//ending code block
				sb.append(ServerMarkdownUtils.END_PRE_CODE);
				isInCodeBlock = false;
			}
		}
		else {
			sb.append(line);
			if (!isInCodeBlock)
				sb.append(ServerMarkdownUtils.HTML_LINE_BREAK);
		}
			
		
		return sb.toString();
	}

	@Override
	public void completeParse(StringBuilder html) {
	}
}
