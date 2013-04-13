package org.sagebionetworks.web.client.utils;

import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.HTMLPanel;

/**
 * Extracted from DisaplyUtils.
 *
 */
public class MarkdownUtils {

	public static void loadTableSorters(final HTMLPanel panel, SynapseJSNIUtils synapseJSNIUtils) {
		String id = WidgetConstants.MARKDOWN_TABLE_ID_PREFIX;
		int i = 0;
		Element table = panel.getElementById(id + i);
		while (table != null) {
			synapseJSNIUtils.tablesorter(id+i);
			i++;
			table = panel.getElementById(id + i);
		}
	}

	public static String getWidgetMD(String attachmentName) {
		if (attachmentName == null)
			return null;
		StringBuilder sb = new StringBuilder();
		sb.append(WidgetConstants.WIDGET_START_MARKDOWN);
		sb.append(attachmentName);
		sb.append("}");
		return sb.toString();
	}

}
