package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface SharingAndDataUseConditionWidgetView extends IsWidget, SynapseView {
	void configure(EntityBundle bundle);
}
