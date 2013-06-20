package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class CodeParser extends BasicMarkdownElementParser  {
	Pattern p;
	boolean isInCodeBlock;
	@Override
	public void init() {
		p = Pattern.compile(MarkdownRegExConstants.FENCE_CODE_BLOCK_REGEX);
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
				sb.append(m.group(1)); //prefix group
				sb.append(ServerMarkdownUtils.START_PRE_CODE);
				if (m.groupCount() == 2)
					sb.append(" class=\""+m.group(2).toLowerCase()+"\"");
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
			if (isInCodeBlock)
				sb.append("\n");
		}
			
		
		return sb.toString();
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
