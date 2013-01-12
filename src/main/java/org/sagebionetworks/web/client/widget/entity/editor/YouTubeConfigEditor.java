package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetNameProvider;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class YouTubeConfigEditor implements YouTubeConfigView.Presenter, WidgetEditorPresenter {
	
	private YouTubeConfigView view;
	private YouTubeWidgetDescriptor descriptor;
	@Inject
	public YouTubeConfigEditor(YouTubeConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof YouTubeWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		descriptor = (YouTubeWidgetDescriptor)widgetDescriptor;
		String videoId = descriptor.getVideoId();
		if (videoId != null)
			view.setVideoUrl(DisplayUtils.getYouTubeVideoUrl(descriptor.getVideoId()));
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
		//update widget descriptor from the view
		view.checkParams();
		descriptor.setVideoId(DisplayUtils.getYouTubeVideoId(view.getVideoUrl()));
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
	public String getTextToInsert() {
		return null;
	}
	
	/*
	 * Private Methods
	 */
}
