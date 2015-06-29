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

public class VimeoConfigEditor implements IFrameConfigView.Presenter, WidgetEditorPresenter {

	private IFrameConfigView view;
	private Map<String, String> descriptor;
	
	@Inject
	public VimeoConfigEditor(IFrameConfigView view) {
		this.view = view;
		view.setPresenter(this);
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
			view.setVideoUrl(DisplayUtils.getVimeoVideoUrl(videoId));
	}

	@Override
	public void updateDescriptorFromView() throws IllegalArgumentException {
		//update widget descriptor from the view
		view.checkParams();
		descriptor.put(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY, DisplayUtils.getVimeoVideoId(view.getVideoUrl()));
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
