package org.sagebionetworks.web.server.markdownparser;

import java.util.ArrayList;
import java.util.List;

/**
 * Used for complex multi-line parsers, to maintain order (prependElement adds the new html tag element to the end of the prepended tag list, for example), and to simply regex (so that parsers do not need to recognize all of the output tags).
 * @author jayhodgson
 *
 */
public class MarkdownElements {
	private List<String> before;
	private List<String> after;
	private String theMarkdown;
	
	public MarkdownElements(String markdown) {
		super();
		this.theMarkdown = markdown;
		before = new ArrayList<String>();
		after = new ArrayList<String>();
	}

	public void appendElement(String element) {
		after.add(element);
	}
	
	public void prependElement(String element) {
		before.add(element);
	}
	
	public String getMarkdown() {
		return theMarkdown;
	}
	
	public void updateMarkdown(String markdown) {
		theMarkdown = markdown;
	}
	
	public String getHtml() {
		StringBuilder sb = new StringBuilder();
		for (String element : before) {
			sb.append(element);
		}
		sb.append(theMarkdown);
		for (String element : after) {
			sb.append(element);
		}
		return sb.toString();
	}
}
