package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Map;

import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.shared.WidgetConstants;
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
		String height = descriptor.get(WidgetConstants.HEIGHT_KEY);
		view.configure(mp4SynapseId, oggSynapseId, webmSynapseId, width, height, authenticationController.getCurrentXsrfToken());
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
	}
	
	public void configure(String synapseId, String filename) {
		String mp4SynapseId = VideoConfigEditor.isRecognizedMP4FileName(filename) ? synapseId : null;
		String oggSynapseId = VideoConfigEditor.isRecognizedOggFileName(filename) ? synapseId : null;
		String webmSynapseId = VideoConfigEditor.isRecognizedWebMFileName(filename) ? synapseId : null;
		view.configure(mp4SynapseId, oggSynapseId, webmSynapseId, null, null, authenticationController.getCurrentXsrfToken());
		
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
