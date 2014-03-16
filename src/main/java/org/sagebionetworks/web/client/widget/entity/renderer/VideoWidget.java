package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.markdown.constants.WidgetConstants;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VideoWidget implements VideoWidgetView.Presenter, WidgetRendererPresenter {
	
	private VideoWidgetView view;
	private Map<String,String> descriptor;
	AuthenticationController authenticationController;
	
	@Inject
	public VideoWidget(VideoWidgetView view, 
			AuthenticationController authenticationController) {
		this.view = view;
		this.authenticationController = authenticationController;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;
		String mp4SynapseId = descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY);
		String oggSynapseId = descriptor.get(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY);
		String webmSynapseId = descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY);
		String width = descriptor.get(WidgetConstants.VIDEO_WIDGET_WIDTH_KEY);
		String height = descriptor.get(WidgetConstants.VIDEO_WIDGET_HEIGHT_KEY);
		view.configure(wikiKey,	mp4SynapseId, oggSynapseId, webmSynapseId, width, height, authenticationController.isLoggedIn(), wikiVersionInView);
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
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
