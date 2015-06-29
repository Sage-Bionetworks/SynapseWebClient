package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VimeoWidget implements IFrameWidgetView.Presenter {

	private IFrameWidgetView view;
	private Map<String, String> descriptor;
	
	@Inject
	public VimeoWidget(IFrameWidgetView view) {
		this.view = view;
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor,
			Callback widgetRefreshRequired, Long wikiVersionInView) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		view.configure("https://player.vimeo.com/video/" + descriptor.get(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY));
	}
}
