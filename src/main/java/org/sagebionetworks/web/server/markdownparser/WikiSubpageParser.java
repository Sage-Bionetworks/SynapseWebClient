package org.sagebionetworks.web.server.markdownparser;

import org.sagebionetworks.web.client.DisplayUtils;

public class WikiSubpageParser extends BasicMarkdownElementParser  {
	String subpagesWidgetMarkdown = DisplayUtils.getWikiSubpagesMarkdown();
	boolean seenWikiSubpagesWidget;
	
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
