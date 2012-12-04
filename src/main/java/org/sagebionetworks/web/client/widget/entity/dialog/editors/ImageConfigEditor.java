package org.sagebionetworks.web.client.widget.entity.dialog.editors;

import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.WidgetDescriptorPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigEditor implements ImageConfigView.Presenter, WidgetDescriptorPresenter {
	
	private ImageConfigView view;
	private ImageAttachmentWidgetDescriptor descriptor;
	
	@Inject
	public ImageConfigEditor(ImageConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}		
	@Override
	public void setWidgetDescriptor(WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof ImageAttachmentWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		//TODO: set up view based on descriptor parameters
		descriptor = (ImageAttachmentWidgetDescriptor)widgetDescriptor;
		//view.setStuff(descriptor.getStuff());
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
		//TODO: update widget descriptor from the view
	}

	@Override
	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}
	/*
	 * Private Methods
	 */
}
