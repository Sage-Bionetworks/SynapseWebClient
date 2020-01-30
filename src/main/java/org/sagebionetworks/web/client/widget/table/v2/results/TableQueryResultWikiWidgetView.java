package org.sagebionetworks.web.client.widget.table.v2.results;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TableQueryResultWikiWidgetView extends IsWidget {
	void setTableQueryResultWidget(Widget tableQueryResultWidget);

	void setSynAlert(Widget synAlert);
}
