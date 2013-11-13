package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WebConstants;

public class SharedMarkdownUtils {

	public static String getWidgetHTML(String id, String widgetProperties){
		boolean inlineWidget = false;
		StringBuilder sb = new StringBuilder();
		if(widgetProperties.contains(WidgetConstants.INLINE_WIDGET_KEY + "=true")) {
			inlineWidget = true;
		}
	
		sb.append("<span id=\"");
		sb.append(WebConstants.DIV_ID_WIDGET_PREFIX);
		sb.append(id);
		
		//Some widgets will be inline
		if(inlineWidget) {
			sb.append("\" class=\"inlineWidgetContainer\" widgetParams=\"");
		} else {
			sb.append("\" class=\"widgetContainer\" widgetParams=\"");
		}
		
		sb.append(widgetProperties);
		sb.append("\">");
		sb.append("</span>");
	    return sb.toString();
	}

	public static String getPreviewSuffix(Boolean isPreview) {
		return isPreview ? WebConstants.DIV_ID_PREVIEW_SUFFIX : "";
	}

}
