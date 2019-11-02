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

public class VideoWidget implements WidgetRendererPresenter {

	public static final String PLEASE_LOGIN_TO_VIEW_THIS_RESOURCE = "Please login to view this resource.";
	public static final String VIMEO_URL_PREFIX = "https://player.vimeo.com/video/";
	public static final String YOUTUBE_URL_PREFIX = "https://www.youtube.com/embed/";
	private VideoWidgetView view;
	private Map<String, String> descriptor;
	AuthenticationController authenticationController;

	@Inject
	public VideoWidget(VideoWidgetView view, AuthenticationController authenticationController) {
		this.view = view;
		this.authenticationController = authenticationController;
	}

	@Override
	public void configure(final WikiPageKey wikiKey, final Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		this.descriptor = widgetDescriptor;

		String youTubeVideoId = descriptor.get(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY);
		String vimeoVideoId = descriptor.get(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY);
		String mp4SynapseId = descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY);
		String oggSynapseId = descriptor.get(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY);
		String webmSynapseId = descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY);
		String width = descriptor.get(WidgetConstants.VIDEO_WIDGET_WIDTH_KEY);
		String height = descriptor.get(WidgetConstants.HEIGHT_KEY);

		if (youTubeVideoId != null) {
			view.configure(YOUTUBE_URL_PREFIX + youTubeVideoId);
		} else if (vimeoVideoId != null) {
			view.configure(VIMEO_URL_PREFIX + vimeoVideoId);
		} else {
			configureFromSynapseFile(mp4SynapseId, oggSynapseId, webmSynapseId, width, height);
		}
		descriptor = widgetDescriptor;
	}

	public void configure(String synapseId, String filename, int width, int height) {
		String mp4SynapseId = VideoConfigEditor.isRecognizedMP4FileName(filename) ? synapseId : null;
		String oggSynapseId = VideoConfigEditor.isRecognizedOggFileName(filename) ? synapseId : null;
		String webmSynapseId = VideoConfigEditor.isRecognizedWebMFileName(filename) ? synapseId : null;
		configureFromSynapseFile(mp4SynapseId, oggSynapseId, webmSynapseId, Integer.toString(width), Integer.toString(height));
	}

	private void configureFromSynapseFile(String mp4SynapseId, String oggSynapseId, String webmSynapseId, String width, String height) {
		if (!authenticationController.isLoggedIn()) {
			// not logged in and attempting to download a Synapse video file.
			view.showError(PLEASE_LOGIN_TO_VIEW_THIS_RESOURCE);
		} else {
			view.configure(mp4SynapseId, oggSynapseId, webmSynapseId, width, height);
		}
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}
}
