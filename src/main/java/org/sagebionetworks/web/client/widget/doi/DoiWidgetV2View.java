package org.sagebionetworks.web.client.widget.doi;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface DoiWidgetV2View extends IsWidget, SynapseView {
	void showDoi(String doiText);
	void hide();
}
