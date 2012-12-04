package org.sagebionetworks.web.client.widget.entity.dialog;

import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.ProvenanceWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.WidgetDescriptorPresenter;


public class WidgetDescriptorUtils {
	
	public static PortalGinInjector ginInjector;
	
	/**
	 * Given a widget class, returns the widget that can be used to edit the WidgetDescriptor (model) for that widget type.
	 * @param widgetClass
	 * @return
	 */
	public static WidgetDescriptorPresenter getWidgetEditorForWidgetDescriptor(WidgetDescriptor model) { 
		//use gin to create a new instance of the proper class.
		WidgetDescriptorPresenter presenter = null;
		String widgetClass = model.getEntityType();
		if (widgetClass.equals(YouTubeWidgetDescriptor.class.getName())) {
			presenter = ginInjector.getYouTubeConfigEditor();
		} else if (widgetClass.equals(ProvenanceWidgetDescriptor.class.getName())) {
			presenter = ginInjector.getProvenanceConfigEditor();
		} else if (widgetClass.equals(ImageAttachmentWidgetDescriptor.class.getName())) {
			presenter = ginInjector.getImageConfigEditor();
		} //TODO: add other widget descriptors to this mapping as they become available
		
		if (presenter != null)
			presenter.setWidgetDescriptor(model);
		return presenter;
	}
	
	public static String getFriendlyTypeName(String widgetClass) {
		String friendlyTypeName = "Widget";
		if (widgetClass.equals(YouTubeWidgetDescriptor.class.getName())) {
			friendlyTypeName = "YouTube";
		} else if (widgetClass.equals(ProvenanceWidgetDescriptor.class.getName())) {
			friendlyTypeName = "Provenance_Graph";
		} else if (widgetClass.equals(ImageAttachmentWidgetDescriptor.class.getName())) {
			friendlyTypeName = "Image";
		} //TODO: add other widget descriptors to this mapping as they become available
		return friendlyTypeName;
	}
}
