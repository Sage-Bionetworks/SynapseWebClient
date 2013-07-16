package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;

public class SharedMarkdownUtils {

	public static String getWikiSubpagesMarkdown() {
		return WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.WIKI_SUBPAGES_CONTENT_TYPE + WidgetConstants.WIDGET_END_MARKDOWN;
	}
	
	public static String getNoAutoWikiSubpagesMarkdown() {
		return WidgetConstants.WIDGET_START_MARKDOWN + WidgetConstants.NO_AUTO_WIKI_SUBPAGES + WidgetConstants.WIDGET_END_MARKDOWN;
	}
	
	public static String getWidgetHTML(int widgetIndex, String suffix, String widgetProperties){
		StringBuilder sb = new StringBuilder();
		sb.append("<div id=\"");
		sb.append(WebConstants.DIV_ID_WIDGET_PREFIX);
		sb.append(widgetIndex);
		sb.append(suffix);
		sb.append("\" class=\"widgetContainer\" widgetParams=\"");
		sb.append(widgetProperties);
		sb.append("\">");
		sb.append("</div>");
	    return sb.toString();
	}

	public static String getDefaultWikiMarkdown() {
		return getWidgetHTML(0, "", WidgetConstants.WIKI_SUBPAGES_CONTENT_TYPE);
	}

}
