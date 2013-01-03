package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetNameProvider;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigEditor implements ImageConfigView.Presenter, WidgetEditorPresenter {
	
	private ImageConfigView view;
	private ImageAttachmentWidgetDescriptor descriptor;
	private WidgetNameProvider provider;
	
	@Inject
	public ImageConfigEditor(ImageConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}		
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof ImageAttachmentWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		//set up view based on descriptor parameters
		descriptor = (ImageAttachmentWidgetDescriptor)widgetDescriptor;
		view.setEntityId(entityId);
		//if the attachmentData is set then there'a an associated image.  Only show the external url ui if we aren't editing one that already has an attachment
		view.setExternalVisible(descriptor.getImage() == null);
		view.setUploadedAttachmentData(descriptor.getImage());
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		view.checkParams();
		if (!view.isExternal())
			descriptor.setImage(view.getUploadedAttachmentData());
	}
	
	@Override
	public String getTextToInsert(String name) {
		if (view.isExternal())
			return "!["+name+"]("+view.getImageUrl()+")";
		else return null;
	}

	@Override
	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}
	@Override
	public int getAdditionalWidth() {
		return view.getAdditionalWidth();
	}
	
	@Override
	public void setName(String name) {
		//try to strip off the file extension
		if (name != null) {
			int lastIndex = name.lastIndexOf('.');
			if (lastIndex > 0)
				name = name.substring(0, lastIndex);
			provider.setName(name);
		}
	}
	
	@Override
	public void setNameProvider(WidgetNameProvider provider) {
		//when an image is uploaded, also set the name of the widget
		this.provider = provider;
	}
	
	/*
	 * Private Methods
	 */
}
