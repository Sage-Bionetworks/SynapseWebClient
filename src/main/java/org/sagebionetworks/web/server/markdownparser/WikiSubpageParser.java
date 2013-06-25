package org.sagebionetworks.web.server.markdownparser;

import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;

public class WikiSubpageParser extends BasicMarkdownElementParser  {
	String subpagesWidgetMarkdown = SharedMarkdownUtils.getWikiSubpagesMarkdown();
	boolean seenWikiSubpagesWidget;
	
	@Override
	public void reset() {
		seenWikiSubpagesWidget = false;
	}

	@Override
	public void processLine(MarkdownElements line) {
		if (!seenWikiSubpagesWidget) {
			if (line.getMarkdown().contains(subpagesWidgetMarkdown))
				seenWikiSubpagesWidget = true;
		}
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		if (!seenWikiSubpagesWidget) {
			html.insert(0,  subpagesWidgetMarkdown + "<br />");
		}
	}
}
