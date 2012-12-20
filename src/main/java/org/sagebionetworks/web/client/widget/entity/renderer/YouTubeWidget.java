package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class YouTubeWidget implements YouTubeWidgetView.Presenter, WidgetRendererPresenter {
	
	private YouTubeWidgetView view;
	private YouTubeWidgetDescriptor descriptor;
	
	@Inject
	public YouTubeWidget(YouTubeWidgetView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof YouTubeWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		//set up view based on descriptor parameters
		descriptor = (YouTubeWidgetDescriptor)widgetDescriptor;
		view.configure(entityId, descriptor.getVideoId());
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
