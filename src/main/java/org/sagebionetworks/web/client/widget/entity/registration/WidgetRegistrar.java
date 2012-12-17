package org.sagebionetworks.web.client.widget.entity.registration;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;

public interface WidgetRegistrar {
	public void registerWidget(String contentTypeKey, String descriptorClassName, String friendlyName);
	public String getWidgetClass(String contentTypeKey);
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(String entityId, String contentTypeKey, WidgetDescriptor model);
	public WidgetRendererPresenter getWidgetRendererForWidgetDescriptor(String entityId, String contentTypeKey, WidgetDescriptor model);
	public String getFriendlyTypeName(String contentTypeKey);
	public boolean isWidgetContentType(String contentTypeKey);
}
