package org.sagebionetworks.web.client.widget.entity.editor;

import static org.sagebionetworks.repo.model.EntityBundle.FILE_NAME;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class VideoConfigEditor implements VideoConfigView.Presenter, WidgetEditorPresenter {
	
	public static final String UNRECOGNIZED_VIDEO_FORMAT_MESSAGE = "Unrecognized video format";
	private VideoConfigView view;
	private Map<String, String> descriptor;
	private SynapseClientAsync synapseClient;
	public static enum VIDEO_TYPE { MP4, OGG, WEBM }
	
	private static final String[] MP4_EXTENSIONS = new String[] {".mp4", ".m4a", ".m4p", ".m4b", ".m4r", ".m4v"};
	private static final HashSet<String> MP4_EXTENSIONS_SET = new HashSet<String>(Arrays.asList(MP4_EXTENSIONS));
	
	private static final String[] OGG_EXTENSIONS = new String[] {".ogv", ".ogg"};
	private static final HashSet<String> OGG_EXTENSIONS_SET = new HashSet<String>(Arrays.asList(OGG_EXTENSIONS));
	VIDEO_TYPE currentType;
	
	@Inject
	public VideoConfigEditor(VideoConfigView view, SynapseClientAsync synapseClient) {
		this.view = view;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
		view.initView();
	}		

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;		
		if (descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY) != null) {
			currentType = VIDEO_TYPE.MP4;
			view.setEntity(descriptor.get(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY));
		} else if (descriptor.get(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY) != null){
			currentType = VIDEO_TYPE.OGG;
			view.setEntity(descriptor.get(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY));
		} else if (descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY) != null){
			currentType = VIDEO_TYPE.WEBM;
			view.setEntity(descriptor.get(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY));
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
		//determine what video type this is
		view.setVideoFormatWarningVisible(false);
		final String entityId = ref.getTargetId();
		int mask = FILE_NAME;
		AsyncCallback<EntityBundle> ebCallback = new AsyncCallback<EntityBundle> () {
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
		synapseClient.getEntityBundle(ref.getTargetId(), mask, ebCallback);
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
		//update widget descriptor from the view
		view.checkParams();
		String entityId = view.getEntity();
		descriptor.clear();
		if (VIDEO_TYPE.MP4.equals(currentType)) {
			descriptor.put(WidgetConstants.VIDEO_WIDGET_MP4_SYNAPSE_ID_KEY, entityId);
		} else if (VIDEO_TYPE.OGG.equals(currentType)) {
			descriptor.put(WidgetConstants.VIDEO_WIDGET_OGG_SYNAPSE_ID_KEY, entityId);
		} else if (VIDEO_TYPE.WEBM.equals(currentType)) {
			descriptor.put(WidgetConstants.VIDEO_WIDGET_WEBM_SYNAPSE_ID_KEY, entityId);
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
	/*
	 * Private Methods
	 */
}
