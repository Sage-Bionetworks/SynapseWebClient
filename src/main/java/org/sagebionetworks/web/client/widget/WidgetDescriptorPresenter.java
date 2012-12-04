package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;


public interface WidgetDescriptorPresenter extends SynapseWidgetPresenter {
	public void setWidgetDescriptor(WidgetDescriptor widgetDescriptor);
	public void updateDescriptorFromView();
	public int getDisplayHeight();
}
