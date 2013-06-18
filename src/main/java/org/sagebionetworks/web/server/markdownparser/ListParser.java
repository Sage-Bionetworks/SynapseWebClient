package org.sagebionetworks.web.server.markdownparser;

import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
/**
 * One of the more complicated parsers.  Needs to have a stack to support nested lists.
 * needs to remember the level (how deep is the nested list), the list type (ordered or unordered), and (if ordered) the current number)
 * @author jayhodgson
 *
 */
public class ListParser implements MarkdownElementParser {
	
	Pattern p1;
	public static final String LIST_REGEX = "^(\\s*)((?:[-+*]|\\d+[.]))(.+)";
	Stack<MarkdownList> stack;
	@Override
	public void init() {
		p1 = Pattern.compile(LIST_REGEX);
	}

	@Override
	public void reset() {
		stack = new Stack<MarkdownList>();
	}

	@Override
	public String processLine(String line) {
		Matcher m = p1.matcher(line);
		if (m.matches()) {
			//looks like a list item
			String spaces = m.group(1);
            int depth = spaces.length();
            String listMarker = m.group(2);
            String value = m.group(3);
            //is it part of the current list?
			if (!stack.isEmpty()) {
				MarkdownList currentList = stack.peek();
				if (currentList.getDepth() == depth) {
					return currentList.getListItemHtml(value);
				}
			}
            
            return "<" + tag + ">" + headingText + "</" + tag + ">";
		}
		return line;
	}
	
	@Override
	public void completeParse(StringBuilder html) {
	}

}
