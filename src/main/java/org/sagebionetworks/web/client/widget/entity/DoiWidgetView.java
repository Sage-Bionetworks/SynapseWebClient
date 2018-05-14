package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface DoiWidgetView extends IsWidget, SynapseView {
	void showDoiCreated(String doiText);
	void showDoiInProgress();
	void showDoiError();
	void setVisible(boolean visible);
	void setSynAlert(IsWidget w);
}
