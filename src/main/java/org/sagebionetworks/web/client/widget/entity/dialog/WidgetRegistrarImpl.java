package org.sagebionetworks.web.client.widget.entity.dialog;

import java.util.HashMap;

import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.ProvenanceWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.WidgetDescriptorPresenter;
import org.sagebionetworks.web.client.widget.provenance.Dimension;

import com.google.inject.Inject;


public class WidgetRegistrarImpl implements WidgetRegistrar {
	
	public static PortalGinInjector ginInjector;
	private HashMap<String, WidgetRegistration> contentType2WidgetInfo = new HashMap<String, WidgetRegistration>();
	
	@Inject
	public WidgetRegistrarImpl() {
		initWithKnownWidgets();
	}
	
	@Override
	public void registerWidget(String contentTypeKey, String friendlyName, String descriptorClassName, Dimension size) {
		WidgetRegistration r = new WidgetRegistration(descriptorClassName, friendlyName, size);
		contentType2WidgetInfo.put(contentTypeKey, r);
	}
	
	@Override
	public String getWidgetClass(String contentTypeKey) {
		WidgetRegistration r = contentType2WidgetInfo.get(contentTypeKey);
		if (r != null)
			return r.getClassName();
		else return null;
	}
	
	@Override
	public Dimension getWidgetSize(String contentTypeKey) {
		WidgetRegistration r = contentType2WidgetInfo.get(contentTypeKey);
		if (r != null)
			return r.getSize();
		else return null;
	}
	
	/**
	 * Given a widget class, returns the widget that can be used to edit the WidgetDescriptor (model) for that widget type.
	 * @param widgetClass
	 * @return
	 */
	@Override
	public WidgetDescriptorPresenter getWidgetEditorForWidgetDescriptor(String contentTypeKey, WidgetDescriptor model) { 
		//use gin to create a new instance of the proper class.
		WidgetDescriptorPresenter presenter = null;
		if (contentTypeKey.equals(WidgetConstants.YOUTUBE_CONTENT_TYPE)) {
			presenter = ginInjector.getYouTubeConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.PROVENANCE_CONTENT_TYPE)) {
			presenter = ginInjector.getProvenanceConfigEditor();
		} else if (contentTypeKey.equals(WidgetConstants.IMAGE_CONTENT_TYPE)) {
			presenter = ginInjector.getImageConfigEditor();
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.setWidgetDescriptor(model);
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
		registerWidget(WidgetConstants.YOUTUBE_CONTENT_TYPE, WidgetConstants.YOUTUBE_FRIENDLY_NAME, YouTubeWidgetDescriptor.class.getName(), new Dimension(480,385));
		registerWidget(WidgetConstants.PROVENANCE_CONTENT_TYPE, WidgetConstants.PROVENANCE_FRIENDLY_NAME, ProvenanceWidgetDescriptor.class.getName(), new Dimension(480,385));
		registerWidget(WidgetConstants.IMAGE_CONTENT_TYPE, WidgetConstants.IMAGE_FRIENDLY_NAME, ImageAttachmentWidgetDescriptor.class.getName(), new Dimension(480,385));
	}

}
