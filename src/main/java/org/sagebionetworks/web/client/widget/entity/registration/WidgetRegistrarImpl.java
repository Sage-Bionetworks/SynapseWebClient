package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;

import com.google.gwt.http.client.URL;
import com.google.inject.Inject;


public class WidgetRegistrarImpl implements WidgetRegistrar {
	
	private HashMap<String, String> contentType2FriendlyName = new HashMap<String, String>();
	
	PortalGinInjector ginInjector;
	NodeModelCreator nodeModelCreator;
	JSONObjectAdapter adapter;
	
	@Inject
	public WidgetRegistrarImpl(PortalGinInjector ginInjector, NodeModelCreator nodeModelCreator, JSONObjectAdapter adapter) {
		this.ginInjector = ginInjector;
		this.nodeModelCreator = nodeModelCreator;
		this.adapter = adapter;
		initWithKnownWidgets();
	}
	
	@Override
	public void registerWidget(String contentTypeKey, String friendlyName) {
		contentType2FriendlyName.put(contentTypeKey, friendlyName);
	}
	
	/**
	 * Given a widget content type, returns the widget that can be used to edit the WidgetDescriptor (model) for that widget type.
	 * @param widgetClass
	 * @return
	 */
	@Override
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(String entityId, String contentTypeKey, Map<String, String> model) { 
		//use gin to create a new instance of the proper class.
		WidgetEditorPresenter presenter = null;
		if (contentTypeKey.equals(WidgetConstants.YOUTUBE_CONTENT_TYPE)) {
			presenter = ginInjector.getYouTubeConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.IMAGE_CONTENT_TYPE)) {
			presenter = ginInjector.getImageConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.LINK_CONTENT_TYPE)) {
			presenter = ginInjector.getLinkConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.API_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseAPICallConfigEditor();
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.configure(entityId, model);
		return presenter;
	}

	@Override
	public String getWidgetContentType(Map<String, String> model) {
		return model.get("contentType");
	}
	
	/**
	 * Given a widget content type, returns the widget that can be used to edit the WidgetDescriptor (model) for that widget type.
	 * @param widgetClass
	 * @return
	 */
	@Override
	public WidgetRendererPresenter getWidgetRendererForWidgetDescriptor(String entityId, String contentTypeKey, Map<String, String> model) { 
		//use gin to create a new instance of the proper class.
		WidgetRendererPresenter presenter = null;
		if (contentTypeKey.equals(WidgetConstants.YOUTUBE_CONTENT_TYPE)) {
			presenter = ginInjector.getYouTubeRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.IMAGE_CONTENT_TYPE)) {
			presenter = ginInjector.getImageRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.API_TABLE_CONTENT_TYPE)) {
			presenter = ginInjector.getSynapseAPICallRenderer();
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.configure(entityId, model);
		return presenter;
	}
	@Override
	public String getFriendlyTypeName(String contentTypeKey) {
		String friendlyName = contentType2FriendlyName.get(contentTypeKey);
		if (friendlyName != null)
			return friendlyName;
		else return "Widget";
	}

	
	@Override
	public String getMDRepresentation(String contentType, Map<String, String> model){
		String decodedMD = getMDRepresentationDecoded(contentType, model);
		return URL.encode(decodedMD);
	}
	
	public String getMDRepresentationDecoded(String contentType, Map<String, String> model) {
		StringBuilder urlBuilder = new StringBuilder();
		urlBuilder.append(contentType);
		char prefix = '?';
		for (Iterator iterator = model.keySet().iterator(); iterator.hasNext();) {
			String key = (String) iterator.next();
			Object value = model.get(key);
			//only include it in the md representation if the value is not null
			if (value != null) {
				urlBuilder.append(prefix).append(key).append('=').append(value);
			}
			prefix = '&';
		}
		return urlBuilder.toString();
	}
	
	@Override
	public Map<String, String> getWidgetDescriptor(String mdRepresentation) {
		if (mdRepresentation == null || mdRepresentation.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + mdRepresentation);
		String decoded = URL.decode(mdRepresentation);
		return getWidgetDescriptorFromDecoded(decoded);
	}
	
	public Map<String, String> getWidgetDescriptorFromDecoded(String decodedMd) {
		if (decodedMd == null || decodedMd.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + decodedMd);
		int delimeter = decodedMd.indexOf("?");
		if (delimeter < 0) {
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + decodedMd);
		}
		String contentTypeKey = decodedMd.substring(0, delimeter);
		String allParamsString = decodedMd.substring(delimeter+1);
		String[] keyValuePairs = allParamsString.split("&");
		Map<String, String> model = new HashMap<String, String>();
		for (int j = 0; j < keyValuePairs.length; j++) {
			String[] keyValue = keyValuePairs[j].split("=");
			model.put(keyValue[0], keyValue[1]);
		}
		return model;
	}

	@Override
	public String getWidgetContentType(String mdRepresentation) {
		if (mdRepresentation == null || mdRepresentation.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + mdRepresentation);
		String decodedMd = URL.decode(mdRepresentation);
		if (decodedMd == null || decodedMd.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + decodedMd);
		int delimeter = decodedMd.indexOf("?");
		if (delimeter < 0) {
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + decodedMd);
		}
		return decodedMd.substring(0, delimeter);
	}
	
	private void initWithKnownWidgets() {
		registerWidget(WidgetConstants.YOUTUBE_CONTENT_TYPE, WidgetConstants.YOUTUBE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.PROVENANCE_CONTENT_TYPE, WidgetConstants.PROVENANCE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.IMAGE_CONTENT_TYPE, WidgetConstants.IMAGE_FRIENDLY_NAME);
		registerWidget(WidgetConstants.LINK_CONTENT_TYPE, WidgetConstants.LINK_FRIENDLY_NAME);
		registerWidget(WidgetConstants.API_TABLE_CONTENT_TYPE, WidgetConstants.API_TABLE_FRIENDLY_NAME);
	}
	
	public static String getWidgetMarkdown(String contentType, Map<String, String> widgetDescriptor, WidgetRegistrar widgetRegistrar) throws JSONObjectAdapterException {
		StringBuilder sb = new StringBuilder();
		sb.append(WidgetConstants.WIDGET_START_MARKDOWN);
		sb.append(widgetRegistrar.getMDRepresentation(contentType, widgetDescriptor));
		sb.append("}");
		return sb.toString();
	}

}
