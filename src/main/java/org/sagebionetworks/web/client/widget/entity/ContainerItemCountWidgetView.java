package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface ContainerItemCountWidgetView extends IsWidget, SynapseView {
	void showCount(Long count);
	void hide();
}
