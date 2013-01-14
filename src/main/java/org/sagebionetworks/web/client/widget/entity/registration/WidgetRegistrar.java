package org.sagebionetworks.web.client.widget.entity.registration;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;

public interface WidgetRegistrar {
	public void registerWidget(String contentTypeKey, String descriptorClassName, String friendlyName);
	public String getWidgetClass(String contentTypeKey);
	public String getWidgetContentType(WidgetDescriptor model);
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(String entityId, String contentTypeKey, WidgetDescriptor model);
	public WidgetRendererPresenter getWidgetRendererForWidgetDescriptor(String entityId, String contentTypeKey, WidgetDescriptor model);
	public String getFriendlyTypeName(String contentTypeKey);
	public boolean isWidgetContentType(String contentTypeKey);
	public WidgetDescriptor getWidgetDescriptor(String mdRepresentation);
	public String getMDRepresentation(WidgetDescriptor model)  throws JSONObjectAdapterException;
}
