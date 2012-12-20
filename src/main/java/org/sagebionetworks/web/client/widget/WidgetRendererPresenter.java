package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;

public interface WidgetRendererPresenter extends SynapseWidgetPresenter {
	/**
	 * This will be called to give you the parent entity ID, and the widget descriptor containing the params that should guide your display
	 * @param entityId
	 * @param widgetDescriptor
	 */
	public void configure(String entityId, WidgetDescriptor widgetDescriptor);
}
