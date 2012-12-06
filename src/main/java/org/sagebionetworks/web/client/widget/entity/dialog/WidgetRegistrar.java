package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.widget.WidgetDescriptorPresenter;
import org.sagebionetworks.web.client.widget.provenance.Dimension;

public interface WidgetRegistrar {
	public void registerWidget(String contentTypeKey, String descriptorClassName, String friendlyName, Dimension size);
	public String getWidgetClass(String contentTypeKey);
	public Dimension getWidgetSize(String contentTypeKey);
	public WidgetDescriptorPresenter getWidgetEditorForWidgetDescriptor(String contentTypeKey, WidgetDescriptor model);
	public String getFriendlyTypeName(String contentTypeKey);
	public boolean isWidgetContentType(String contentTypeKey);
}
