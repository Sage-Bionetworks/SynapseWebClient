package org.sagebionetworks.web.client.widget.biodalliance13.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class BiodallianceEditor implements BiodallianceEditorView.Presenter, WidgetEditorPresenter {
	
	private BiodallianceEditorView view;
	private Map<String, String> descriptor;
	
	@Inject
	public BiodallianceEditor(BiodallianceEditorView view) {
		this.view = view;
		view.initView();
	}
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		String videoId = descriptor.get(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY);
		if (videoId != null)
			view.setVideoUrl("http://www.youtube.com/watch?v=" + videoId);
	}
	
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
