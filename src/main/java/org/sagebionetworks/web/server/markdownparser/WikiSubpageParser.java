package org.sagebionetworks.web.server.markdownparser;

import org.sagebionetworks.web.client.widget.entity.SharedMarkdownUtils;

public class WikiSubpageParser extends BasicMarkdownElementParser  {
	String subpagesWidgetMarkdown = SharedMarkdownUtils.getWikiSubpagesMarkdown();
	String noSubpagesMarkdown = SharedMarkdownUtils.getNoAutoWikiSubpagesMarkdown();
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
			if (line.getMarkdown().contains(noSubpagesMarkdown)) {
				seenWikiSubpagesWidget = true;
				line.updateMarkdown(line.getMarkdown().replace(noSubpagesMarkdown, ""));
			}
		}
	}
	
	@Override
	public void completeParse(StringBuilder html) {
		if (!seenWikiSubpagesWidget) {
			html.insert(0,  subpagesWidgetMarkdown + "<br />");
		}
	}
}
