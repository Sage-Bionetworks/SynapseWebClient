package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.Map;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;

public interface WidgetRegistrar {
	void registerWidget(String contentTypeKey, String friendlyName);

	String getWidgetContentType(Map<String, String> model);

	void getWidgetEditorForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, DialogCallback dialogCallback, AsyncCallback<WidgetEditorPresenter> callback);

	IsWidget getWidgetRendererForWidgetDescriptor(WikiPageKey wikiKey, String contentTypeKey, Map<String, String> model, Callback widgetRefreshRequired, Long wikiVersionInView);

	String getFriendlyTypeName(String contentTypeKey);

	Map<String, String> getWidgetDescriptor(String mdRepresentation);

	String getWidgetContentType(String mdRepresentation);

	String getMDRepresentation(String contentType, Map<String, String> model) throws JSONObjectAdapterException;

	void getWidgetRendererForWidgetDescriptorAfterLazyLoad(String contentTypeKey, AsyncCallback<WidgetRendererPresenter> callback);
}
