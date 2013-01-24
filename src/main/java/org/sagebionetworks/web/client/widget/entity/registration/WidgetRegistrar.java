package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;

public interface WidgetRegistrar {
	public void registerWidget(String contentTypeKey, String friendlyName);
	public String getWidgetContentType(Map<String, String> model);
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(String ownerObjectId, String ownerObjectType, String contentTypeKey, Map<String, String> model);
	public WidgetRendererPresenter getWidgetRendererForWidgetDescriptor(String ownerObjectId, String ownerObjectType, String contentTypeKey, Map<String, String> model);
	public String getFriendlyTypeName(String contentTypeKey);
	public Map<String, String> getWidgetDescriptor(String mdRepresentation);
	public String getWidgetContentType(String mdRepresentation);
	public String getMDRepresentation(String contentType, Map<String, String> model)  throws JSONObjectAdapterException;
}
