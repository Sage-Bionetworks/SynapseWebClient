package org.sagebionetworks.web.client.widget.doi;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface DoiWidgetV2View extends IsWidget, SynapseView {
	void showDoiCreated(String doiText);
	void setVisible(boolean visible);
	void setSynAlert(IsWidget w);
}
