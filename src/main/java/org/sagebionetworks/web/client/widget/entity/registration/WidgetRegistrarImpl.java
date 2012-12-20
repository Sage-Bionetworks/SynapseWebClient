package org.sagebionetworks.web.client.widget.entity.registration;

import java.util.HashMap;

import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.ProvenanceWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetFactory;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;

import com.google.inject.Inject;


public class WidgetRegistrarImpl implements WidgetRegistrar {
	
	private WidgetFactory widgetFactory;
	private HashMap<String, WidgetRegistration> contentType2WidgetInfo = new HashMap<String, WidgetRegistration>();
	
	@Inject
	public WidgetRegistrarImpl(WidgetFactory widgetFactory) {
		initWithKnownWidgets();
		this.widgetFactory = widgetFactory;
	}
	
	@Override
	public void registerWidget(String contentTypeKey, String friendlyName, String descriptorClassName) {
		WidgetRegistration r = new WidgetRegistration(descriptorClassName, friendlyName);
		contentType2WidgetInfo.put(contentTypeKey, r);
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
			presenter = widgetFactory.createYouTubeWidgetEditor();
		} else if (contentTypeKey.equals(WidgetConstants.PROVENANCE_CONTENT_TYPE)) {
			presenter = widgetFactory.createProvenanceWidgetEditor();
		} else if (contentTypeKey.equals(WidgetConstants.IMAGE_CONTENT_TYPE)) {
			presenter = widgetFactory.createImageWidgetEditor();
		} else if (contentTypeKey.equals(WidgetConstants.LINK_CONTENT_TYPE)) {
			presenter = widgetFactory.createLinkWidgetEditor();
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.configure(entityId, model);
		return presenter;
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
			presenter = widgetFactory.createYouTubeWidget();
		} else if (contentTypeKey.equals(WidgetConstants.PROVENANCE_CONTENT_TYPE)) {
			presenter = widgetFactory.createProvenanceWidget();
		} else if (contentTypeKey.equals(WidgetConstants.IMAGE_CONTENT_TYPE)) {
			presenter = widgetFactory.createImageWidget();
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
	
	private void initWithKnownWidgets() {
		registerWidget(WidgetConstants.YOUTUBE_CONTENT_TYPE, WidgetConstants.YOUTUBE_FRIENDLY_NAME, YouTubeWidgetDescriptor.class.getName());
		registerWidget(WidgetConstants.PROVENANCE_CONTENT_TYPE, WidgetConstants.PROVENANCE_FRIENDLY_NAME, ProvenanceWidgetDescriptor.class.getName());
		registerWidget(WidgetConstants.IMAGE_CONTENT_TYPE, WidgetConstants.IMAGE_FRIENDLY_NAME, ImageAttachmentWidgetDescriptor.class.getName());
		registerWidget(WidgetConstants.LINK_CONTENT_TYPE, WidgetConstants.LINK_FRIENDLY_NAME, null);
	}

}
