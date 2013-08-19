package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class MathParser extends BasicMarkdownElementParser  {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.FENCE_MATH_BLOCK_REGEX);
	Pattern p2 = Pattern.compile(MarkdownRegExConstants.MATH_SPAN_REGEX);
	
	boolean isInMathBlock, isFirstMathLine;
	int mathElementCount;
	
	@Override
	public void reset() {
		isInMathBlock = false;
		isFirstMathLine = false;
		mathElementCount = -1;
	}

	private String getNewMathElementStart() {
		mathElementCount++;
		StringBuilder sb = new StringBuilder();
		sb.append(ServerMarkdownUtils.START_MATH);
		sb.append(mathElementCount);
		sb.append("\">");
		return sb.toString();
	}
	
	@Override
	public void processLine(MarkdownElements line) {
		Matcher m;
		//math block
		m = p1.matcher(line.getMarkdown());
		if (m.matches()) {
			if (!isInMathBlock) {
				//starting math block
				isInMathBlock = true;
				isFirstMathLine = true;
				line.prependElement(getNewMathElementStart());
			}
			else {
				//ending math block
				line.appendElement(ServerMarkdownUtils.END_MATH);
				isInMathBlock = false;
			}
			//remove all fenced blocks from the markdown, just set to the prefix group
			line.updateMarkdown(m.group(1));
		}
		else {
			if (isInMathBlock) {
				if (!isFirstMathLine) {
					line.prependElement("\n");	
				}
			}
			//TODO: fix inline math spans. Need a way to protect equation. 
			//One idea is to store the equation text (group 2) in a map that links div-id2equation, 
			//then in the completeParse construct the html doc find the target divs, and set the child node text to the (protected) equation.
//			else {
//				//not currently in a math block, and this is not the start/end of a math block
//				//check for math span
//				m = p2.matcher(line.getMarkdown());
//				StringBuffer sb = new StringBuffer();
//				while(m.find()) {
//					String updated = getNewMathElementStart() + m.group(2) + ServerMarkdownUtils.END_MATH;
//					m.appendReplacement(sb, updated);
//				}
//				m.appendTail(sb);
//				line.updateMarkdown(sb.toString());		
//			}
			if (isFirstMathLine)
				isFirstMathLine = false;
		}		
	}

	@Override
	public boolean isInMarkdownElement() {
		return isInMathBlock;
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
