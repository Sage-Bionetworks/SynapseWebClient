package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class YouTubeConfigEditor implements IFrameConfigView.Presenter, WidgetEditorPresenter {
	
	private IFrameConfigView view;
	private Map<String, String> descriptor;
	
	@Inject
	public YouTubeConfigEditor(IFrameConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		String videoId = descriptor.get(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY);
		if (videoId != null)
			view.setVideoUrl("http://www.youtube.com/watch?v=" + videoId);
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
		descriptor.put(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY, getYouTubeVideoId(view.getVideoUrl()));
	}
	
	public String getYouTubeVideoId(String videoUrl) {
		String videoId = null;
		//parse out the video id from the url
		int start = videoUrl.indexOf("v=");
		if (start > -1) {
			int end = videoUrl.indexOf("&", start);
			if (end == -1)
				end = videoUrl.length();
			videoId = videoUrl.substring(start + "v=".length(), end);
		}
		if (videoId == null || videoId.trim().length() == 0) {
			throw new IllegalArgumentException("Could not determine the YouTube video ID from the given URL.");
		}
		return videoId;
	}
	
	
	@Override
	public String getTextToInsert() {
		return null;
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}
	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}
	/*
	 * Private Methods
	 */
}
