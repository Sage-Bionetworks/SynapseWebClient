package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.HashMap;
import java.util.Iterator;

import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.ProvenanceWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
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
	
	private HashMap<String, WidgetRegistration> contentType2WidgetInfo = new HashMap<String, WidgetRegistration>();
	private HashMap<String, String> widgetDescriptorClass2ContentType = new HashMap<String, String>();
	
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
	public void registerWidget(String contentTypeKey, String friendlyName, String descriptorClassName) {
		WidgetRegistration r = new WidgetRegistration(descriptorClassName, friendlyName);
		contentType2WidgetInfo.put(contentTypeKey, r);
		widgetDescriptorClass2ContentType.put(descriptorClassName, contentTypeKey);
	}
	
	@Override
	public String getWidgetClass(String contentTypeKey) {
		WidgetRegistration r = contentType2WidgetInfo.get(contentTypeKey);
		if (r != null)
			return r.getClassName();
		else return null;
	}
	
	/**
	 * Given a widget content type, returns the widget that can be used to edit the WidgetDescriptor (model) for that widget type.
	 * @param widgetClass
	 * @return
	 */
	@Override
	public WidgetEditorPresenter getWidgetEditorForWidgetDescriptor(String entityId, String contentTypeKey, WidgetDescriptor model) { 
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
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.configure(entityId, model);
		return presenter;
	}

	@Override
	public String getWidgetContentType(WidgetDescriptor model) {
		return widgetDescriptorClass2ContentType.get(model.getClass().getName());
	}
	
	/**
	 * Given a widget content type, returns the widget that can be used to edit the WidgetDescriptor (model) for that widget type.
	 * @param widgetClass
	 * @return
	 */
	@Override
	public WidgetRendererPresenter getWidgetRendererForWidgetDescriptor(String entityId, String contentTypeKey, WidgetDescriptor model) { 
		//use gin to create a new instance of the proper class.
		WidgetRendererPresenter presenter = null;
		if (contentTypeKey.equals(WidgetConstants.YOUTUBE_CONTENT_TYPE)) {
			presenter = ginInjector.getYouTubeRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceRenderer();
		} else if (contentTypeKey.equals(WidgetConstants.IMAGE_CONTENT_TYPE)) {
			presenter = ginInjector.getImageRenderer();
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.configure(entityId, model);
		return presenter;
	}
	@Override
	public String getFriendlyTypeName(String contentTypeKey) {
		WidgetRegistration r = contentType2WidgetInfo.get(contentTypeKey);
		if (r != null)
			return r.getFriendlyName();
		else return "Widget";
	}

	@Override
	public boolean isWidgetContentType(String contentTypeKey) {
		return contentType2WidgetInfo.containsKey(contentTypeKey);
	}
	
	@Override
	public String getMDRepresentation(WidgetDescriptor model) throws JSONObjectAdapterException {
		String decodedMD = getMDRepresentationDecoded(model);
		return URL.encode(decodedMD);
	}
	
	public String getMDRepresentationDecoded(WidgetDescriptor model) throws JSONObjectAdapterException {
		StringBuilder urlBuilder = new StringBuilder();
		JSONObjectAdapter widgetDescriptorJson = model.writeToJSONObject(adapter.createNew());
		urlBuilder.append(getWidgetContentType(model));
		char prefix = '?';
		for (Iterator iterator = widgetDescriptorJson.keys(); iterator
				.hasNext();) {
			String key = (String) iterator.next();
			Object value = widgetDescriptorJson.get(key);
			//only include it in the md representation if the value is not null
			if (value != null) {
				urlBuilder.append(prefix).append(key).append('=').append(value);
			}
			prefix = '&';
		}
		return urlBuilder.toString();
	}
	
	@Override
	public WidgetDescriptor getWidgetDescriptor(String mdRepresentation) {
		if (mdRepresentation == null || mdRepresentation.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + mdRepresentation);
		String decoded = URL.decode(mdRepresentation);
		return getWidgetDescriptorFromDecoded(decoded);
	}
	
	public WidgetDescriptor getWidgetDescriptorFromDecoded(String decodedMd) {
		if (decodedMd == null || decodedMd.length() == 0) throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + decodedMd);
		int delimeter = decodedMd.indexOf("?");
		if (delimeter < 0) {
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_MARKDOWN_MESSAGE + decodedMd);
		}
		String contentTypeKey = decodedMd.substring(0, delimeter);
		String allParamsString = decodedMd.substring(delimeter+1);
		String[] keyValuePairs = allParamsString.split("&");
		String widgetClassName = getWidgetClass(contentTypeKey);
		WidgetDescriptor widgetDescriptor = (WidgetDescriptor) nodeModelCreator.newInstance(widgetClassName);
		
		JSONObjectAdapter newAdapter = adapter.createNew();
		try {
			for (int j = 0; j < keyValuePairs.length; j++) {
				String[] keyValue = keyValuePairs[j].split("=");
				newAdapter.put(keyValue[0], keyValue[1]);
			}
			
			widgetDescriptor.initializeFromJSONObject(newAdapter);
		} catch (JSONObjectAdapterException e) {
			//if there were any problems with the format of the given parameters in the markdown, it's invalid markdown
			throw new IllegalArgumentException(e);
		}
		return widgetDescriptor;

	}
	
	private void initWithKnownWidgets() {
		registerWidget(WidgetConstants.YOUTUBE_CONTENT_TYPE, WidgetConstants.YOUTUBE_FRIENDLY_NAME, YouTubeWidgetDescriptor.class.getName());
		registerWidget(WidgetConstants.PROVENANCE_CONTENT_TYPE, WidgetConstants.PROVENANCE_FRIENDLY_NAME, ProvenanceWidgetDescriptor.class.getName());
		registerWidget(WidgetConstants.IMAGE_CONTENT_TYPE, WidgetConstants.IMAGE_FRIENDLY_NAME, ImageAttachmentWidgetDescriptor.class.getName());
		registerWidget(WidgetConstants.LINK_CONTENT_TYPE, WidgetConstants.LINK_FRIENDLY_NAME, null);
	}

}
