package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VimeoConfigEditor implements IFrameConfigView.Presenter, WidgetEditorPresenter {

	private IFrameConfigView view;
	private Map<String, String> descriptor;
	
	@Inject
	public VimeoConfigEditor(IFrameConfigView view) {
		this.view = view;
		view.initView();
	}	
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback window) {
		descriptor = widgetDescriptor;
		String videoId = descriptor.get(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY);
		if (videoId != null)
			view.setVideoUrl("https://player.vimeo.com/video/" + videoId);
	}

	@Override
	public void updateDescriptorFromView() throws IllegalArgumentException {
		//update widget descriptor from the view
		view.checkParams();
		descriptor.put(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY, getVimeoVideoId(view.getVideoUrl()));
	}
	
	public String getVimeoVideoId(String videoUrl) {
		String videoId = null;
		//parse out the video id from the urlS
		int start = videoUrl.lastIndexOf("/");
		if (start > -1) {
			videoId = videoUrl.substring(start + 1);
		}
		if (videoId == null || videoId.trim().length() == 0) {
			throw new IllegalArgumentException("Could not determine the Vimeo video ID from the given URL.");
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

}
