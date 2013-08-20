package org.sagebionetworks.web.server.markdownparser;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;
import org.sagebionetworks.web.server.ServerMarkdownUtils;
import org.sagebionetworks.web.shared.WebConstants;

public class MathParser extends BasicMarkdownElementParser  {
	Pattern p1 = Pattern.compile(MarkdownRegExConstants.FENCE_MATH_BLOCK_REGEX);
	Pattern p2 = Pattern.compile(MarkdownRegExConstants.MATH_SPAN_REGEX);
	
	Map<String, String> div2equation = new HashMap<String, String>();
	
	boolean isInMathBlock, isFirstMathLine;
	int mathElementCount;
	
	@Override
	public void reset() {
		isInMathBlock = false;
		isFirstMathLine = false;
		mathElementCount = -1;
		div2equation.clear();
	}

	private String getCurrentDivID() {
		return WebConstants.DIV_ID_MATHJAX_PREFIX + mathElementCount + SharedMarkdownUtils.getPreviewSuffix(isPreview);
	}
	
	private String getNewMathElementStart() {
		mathElementCount++;
		StringBuilder sb = new StringBuilder();
		sb.append(ServerMarkdownUtils.START_CONTAINER + getCurrentDivID());
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
				line.appendElement(ServerMarkdownUtils.END_CONTAINER);
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
			else {
				//not currently in a math block, and this is not the start/end of a math block
				//check for math span
				processMathSpan(line);		
			}
			if (isFirstMathLine)
				isFirstMathLine = false;
		}		
	}

	private void processMathSpan(MarkdownElements line) {
		Matcher m = p2.matcher(line.getMarkdown());
		StringBuffer sb = new StringBuffer();
		while(m.find()) {
			//leave containers to filled in on completeParse()
			String containerElement = getNewMathElementStart() + ServerMarkdownUtils.END_CONTAINER;
			div2equation.put(getCurrentDivID(), m.group(2));
			m.appendReplacement(sb, containerElement);
		}
		m.appendTail(sb);
		line.updateMarkdown(sb.toString());
	}
	
	/**
	 * Fill in the stored equations into the containers that we made during parse
	 */
	@Override
	public void completeParse(Document doc) {
		for (String key : div2equation.keySet()) {
			Element el = doc.getElementById(key);
			//should never be null, but just to be safe
			if (el != null)
				el.appendText(div2equation.get(key));
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
