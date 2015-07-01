package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class YouTubeWidget implements IFrameWidgetView.Presenter {
	
	private IFrameWidgetView view;
	private Map<String, String> descriptor;
	
	@Inject
	public YouTubeWidget(IFrameWidgetView view) {
		this.view = view;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		view.configure("https://www.youtube.com/embed/" + descriptor.get(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY));
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

}
