package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityListWidgetView extends IsWidget {
	void clearRows();

	void addRow(Widget w);

	void setEmptyUiVisible(boolean visible);

	void setTableVisible(boolean visible);

	void setDescriptionHeaderVisible(boolean visible);
}
