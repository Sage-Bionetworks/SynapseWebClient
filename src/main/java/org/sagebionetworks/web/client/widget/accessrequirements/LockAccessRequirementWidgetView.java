package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;

public interface LockAccessRequirementWidgetView extends IsWidget {
	void addStyleNames(String styleNames);

	void setDeleteAccessRequirementWidget(IsWidget w);

	void setSubjectsWidget(IsWidget w);
}
