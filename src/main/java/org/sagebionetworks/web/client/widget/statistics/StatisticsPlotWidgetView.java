package org.sagebionetworks.web.client.widget.statistics;

import com.google.gwt.user.client.ui.IsWidget;

public interface StatisticsPlotWidgetView extends IsWidget {
	void clear();

	void setVisible(boolean visible);

	void configureAndShow(String projectId, String sessionToken);
}
