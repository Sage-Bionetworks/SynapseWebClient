package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityPageTopView extends IsWidget, SynapseView {
	void setProjectMetadata(Widget w);
	void setTabs(Widget w);
	void setProjectActionMenu(Widget w);
	void setEntityActionMenu(Widget w);
	void setProjectInformationVisible(boolean isVisible);
	void setLoadingVisible(boolean visible);
}
