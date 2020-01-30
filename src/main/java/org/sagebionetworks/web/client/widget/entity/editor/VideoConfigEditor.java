package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundle;
import org.sagebionetworks.repo.model.entitybundle.v2.EntityBundleRequest;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class VideoConfigEditor implements VideoConfigView.Presenter, WidgetEditorPresenter {

	public static final String VIMEO_URL_PREFIX = "https://player.vimeo.com/video/";
	public static final String YOUTUBE_URL_PREFIX = "http://www.youtube.com/watch?v=";

	public static final String UNRECOGNIZED_VIDEO_FORMAT_MESSAGE = "Unrecognized video format";
	private VideoConfigView view;
	private Map<String, String> descriptor;
	private SynapseJavascriptClient jsClient;

	public static enum VIDEO_TYPE {
		MP4, OGG, WEBM
	}

	private static final String[] MP4_EXTENSIONS = new String[] {".mp4", ".m4a", ".m4p", ".m4b", ".m4r", ".m4v"};
	private static final HashSet<String> MP4_EXTENSIONS_SET = new HashSet<String>(Arrays.asList(MP4_EXTENSIONS));

	private static final String[] OGG_EXTENSIONS = new String[] {".ogv", ".ogg"};
	private static final HashSet<String> OGG_EXTENSIONS_SET = new HashSet<String>(Arrays.asList(OGG_EXTENSIONS));
	VIDEO_TYPE currentType;

	@Inject
	public VideoConfigEditor(VideoConfigView view, SynapseJavascriptClient jsClient) {
		this.view = view;
		this.jsClient = jsClient;
		view.setPresenter(this);
		view.initView();
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		String vimeoVideoId = descriptor.get(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY);
		String youtubeVideoId = descriptor.get(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY);
		if (vimeoVideoId != null) {
			view.showVimeoTab();
			view.setVimeoVideoUrl(VIMEO_URL_PREFIX + vimeoVideoId);
		} else if (youtubeVideoId != null) {
			view.showYouTubeTab();
			view.setYouTubeVideoUrl(YOUTUBE_URL_PREFIX + youtubeVideoId);
		} else {
			view.showSynapseTab();
			if (descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY) != null) {
				currentType = VIDEO_TYPE.MP4;
				view.setEntity(descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY));
			} else if (descriptor.get(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY) != null) {
				currentType = VIDEO_TYPE.OGG;
				view.setEntity(descriptor.get(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY));
			} else if (descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY) != null) {
				currentType = VIDEO_TYPE.WEBM;
				view.setEntity(descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY));
			}
		}
	}

	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void validateSelection(final Reference ref) {
		// determine what video type this is
		view.setVideoFormatWarningVisible(false);
		final String entityId = ref.getTargetId();
		EntityBundleRequest bundleRequest = new EntityBundleRequest();
		bundleRequest.setIncludeFileName(true);
		AsyncCallback<EntityBundle> ebCallback = new AsyncCallback<EntityBundle>() {
			@Override
			public void onFailure(Throwable caught) {
				view.showFinderError(caught.getMessage());
			}

			@Override
			public void onSuccess(EntityBundle result) {
				currentType = null;
				String fileName = result.getFileName();
				if (isRecognizedMP4FileName(fileName)) {
					currentType = VIDEO_TYPE.MP4;

				} else {
					view.setVideoFormatWarningVisible(true);
					if (isRecognizedOggFileName(fileName)) {
						currentType = VIDEO_TYPE.OGG;
					} else if (isRecognizedWebMFileName(fileName)) {
						currentType = VIDEO_TYPE.WEBM;
					} else {
						view.showFinderError(UNRECOGNIZED_VIDEO_FORMAT_MESSAGE);
					}
				}
				if (currentType != null) {
					view.setEntity(entityId);
					view.hideFinder();
				}
			}

		};
		jsClient.getEntityBundle(ref.getTargetId(), bundleRequest, ebCallback);
	}

	public static boolean isRecognizedVideoFileName(String fileName) {
		String extension = getExtension(fileName);
		return MP4_EXTENSIONS_SET.contains(extension) || ".webm".equals(extension) || OGG_EXTENSIONS_SET.contains(extension);
	}

	public static boolean isRecognizedMP4FileName(String fileName) {
		String extension = getExtension(fileName);
		if (extension != null) {
			return MP4_EXTENSIONS_SET.contains(extension);
		}
		return false;
	}

	public static boolean isRecognizedWebMFileName(String fileName) {
		String extension = getExtension(fileName);
		if (extension != null) {
			return ".webm".equals(extension);
		}
		return false;
	}

	public static boolean isRecognizedOggFileName(String fileName) {
		String extension = getExtension(fileName);
		if (extension != null) {
			return OGG_EXTENSIONS_SET.contains(extension);
		}
		return false;
	}

	private static String getExtension(String fileName) {
		if (fileName != null) {
			int lastDot = fileName.lastIndexOf(".");
			if (lastDot > -1) {
				return fileName.substring(lastDot).toLowerCase();
			}
		}
		return null;
	}


	@Override
	public void updateDescriptorFromView() {
		// update widget descriptor from the view
		view.checkParams();
		descriptor.clear();
		if (view.isSynapseEntity()) {
			String entityId = view.getEntity();
			if (VIDEO_TYPE.MP4.equals(currentType)) {
				descriptor.put(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY, entityId);
			} else if (VIDEO_TYPE.OGG.equals(currentType)) {
				descriptor.put(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY, entityId);
			} else if (VIDEO_TYPE.WEBM.equals(currentType)) {
				descriptor.put(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY, entityId);
			}
		} else if (view.isYouTubeVideo()) {
			descriptor.put(WidgetConstants.YOUTUBE_WIDGET_VIDEO_ID_KEY, getYouTubeVideoId(view.getYouTubeVideoUrl()));
		} else if (view.isVimeoVideo()) {
			descriptor.put(WidgetConstants.VIMEO_WIDGET_VIDEO_ID_KEY, getVimeoVideoId(view.getVimeoVideoUrl()));
		}
	}

	@Override
	public String getTextToInsert() {
		return null;
	}

	/**
	 * TODO: add tab to attach video files to the wiki
	 */
	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}

	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}

	public String getVimeoVideoId(String videoUrl) {
		String videoId = null;
		// parse out the video id from the urlS
		int start = videoUrl.lastIndexOf("/");
		if (start > -1) {
			videoId = videoUrl.substring(start + 1);
		}
		if (videoId == null || videoId.trim().length() == 0) {
			throw new IllegalArgumentException("Could not determine the Vimeo video ID from the given URL.");
		}
		return videoId;
	}


	public String getYouTubeVideoId(String videoUrl) {
		String videoId = null;
		// parse out the video id from the url
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
}
