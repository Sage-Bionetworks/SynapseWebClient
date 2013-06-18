package org.sagebionetworks.web.server.markdownparser;

import java.util.regex.Pattern;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.server.ServerMarkdownUtils;

public class WikiSubpageParser implements MarkdownElementParser {
	String subpagesWidgetMarkdown;
	boolean seenWikiSubpagesWidget;
	
	@Override
	public void init() {
		subpagesWidgetMarkdown = DisplayUtils.getWikiSubpagesMarkdown();
	}

	@Override
	public void reset() {
		seenWikiSubpagesWidget = false;
	}

	@Override
	public String processLine(String line) {
		if (!seenWikiSubpagesWidget) {
			if (line.contains(subpagesWidgetMarkdown))
				seenWikiSubpagesWidget = true;
		}
		return line;
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		if (!seenWikiSubpagesWidget) {
			html.insert(0,  subpagesWidgetMarkdown + "<br />");
		}
	}

}
