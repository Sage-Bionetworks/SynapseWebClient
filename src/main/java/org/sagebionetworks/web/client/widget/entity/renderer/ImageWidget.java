package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.widget.ImageAttachmentWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageWidget implements ImageWidgetView.Presenter, WidgetRendererPresenter {
	
	private ImageWidgetView view;
	private ImageAttachmentWidgetDescriptor descriptor;
	
	@Inject
	public ImageWidget(ImageWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof ImageAttachmentWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		//set up view based on descriptor parameters
		descriptor = (ImageAttachmentWidgetDescriptor)widgetDescriptor;
		view.configure(entityId, descriptor.getImage());
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

		/*
	 * Private Methods
	 */
}
