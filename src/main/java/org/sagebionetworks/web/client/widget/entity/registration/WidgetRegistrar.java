package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.Map;
import com.extjs.gxt.ui.client.widget.Dialog;

import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

public interface WidgetRegistrar {
	public void registerWidget(String contentTypeKey, String friendlyName);
	public String getWidgetContentType(Map<String, String> model);
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, boolean isWiki, Dialog window);
	public WidgetRendererPresenter getWidgetRendererForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, boolean isWiki, Callback widgetRefreshRequired);
	public String getFriendlyTypeName(String contentTypeKey);
	public Map<String, String> getWidgetDescriptor(String mdRepresentation);
	public String getWidgetContentType(String mdRepresentation);
	public String getMDRepresentation(String contentType, Map<String, String> model)  throws JSONObjectAdapterException;
}
