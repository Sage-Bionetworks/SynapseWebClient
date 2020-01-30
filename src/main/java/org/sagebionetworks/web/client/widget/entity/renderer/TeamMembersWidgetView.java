package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface TeamMembersWidgetView extends IsWidget {
	void clearRows();

	void addRow(IsWidget w);

	void setPaginationWidget(IsWidget w);

	void setSynapseAlert(IsWidget w);

	void setLoadingVisible(boolean visible);
}
