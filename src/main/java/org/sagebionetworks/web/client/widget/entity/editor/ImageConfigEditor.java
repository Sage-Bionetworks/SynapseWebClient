package org.sagebionetworks.web.client.widget.entity.editor;

import static org.sagebionetworks.web.shared.WidgetConstants.ALIGNMENT_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_ALT_TEXT_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_RESPONSIVE_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_SCALE_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.IMAGE_WIDGET_SYNAPSE_ID_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.WIDGET_ENTITY_VERSION_KEY;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.upload.FileUpload;
import org.sagebionetworks.web.client.widget.upload.ImageUploadWidget;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ImageConfigEditor implements ImageConfigView.Presenter, WidgetEditorPresenter {

	private ImageConfigView view;
	private Map<String, String> descriptor;
	private FileUpload file;
	private ImageUploadWidget fileInputWidget;
	private WikiAttachments wikiAttachments;
	private List<String> fileHandleIds;

	@Inject
	public ImageConfigEditor(ImageConfigView view, ImageUploadWidget fileInputWidget, WikiAttachments wikiAttachments) {
		this.view = view;
		view.setPresenter(this);
		this.fileInputWidget = fileInputWidget;
		this.wikiAttachments = wikiAttachments;
		view.setFileInputWidget(fileInputWidget.asWidget());
		view.setWikiAttachmentsWidget(wikiAttachments.asWidget());
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, final DialogCallback dialogCallback) {
		fileHandleIds = new ArrayList<String>();
		descriptor = widgetDescriptor;
		this.file = null;
		view.initView();
		view.setWikiAttachmentsWidgetVisible(true);
		fileInputWidget.reset();
		fileInputWidget.configure(new CallbackP<FileUpload>() {
			@Override
			public void invoke(FileUpload fileUpload) {
				view.showUploadSuccessUI(fileUpload.getFileMeta().getFileName());
				// enable the ok button
				dialogCallback.setPrimaryEnabled(true);
				file = fileUpload;
				fileHandleIds.add(file.getFileHandleId());
				view.setWikiAttachmentsWidgetVisible(false);
			}
		});
		if (wikiKey != null) {
			wikiAttachments.configure(wikiKey);
		}
		if (descriptor.containsKey(IMAGE_WIDGET_SYNAPSE_ID_KEY)) {
			// existing synapse id
			view.setWikiFilesTabVisible(false);
			view.showSynapseTab();
			String synId = descriptor.get(IMAGE_WIDGET_SYNAPSE_ID_KEY);
			Long version = null;
			if (descriptor.containsKey(WIDGET_ENTITY_VERSION_KEY)) {
				version = Long.parseLong(descriptor.get(WIDGET_ENTITY_VERSION_KEY));
			}
			view.setSynapseId(DisplayUtils.createEntityVersionString(synId, version));
		} else {
			view.showWikiFilesTab();
			if (descriptor.containsKey(IMAGE_WIDGET_FILE_NAME_KEY)) {
				// existing attachment
				wikiAttachments.setSelectedFilename(descriptor.get(IMAGE_WIDGET_FILE_NAME_KEY));
			}
		}

		if (descriptor.containsKey(ALIGNMENT_KEY)) {
			view.setAlignment(descriptor.get(ALIGNMENT_KEY));
		}
		if (descriptor.containsKey(IMAGE_WIDGET_SCALE_KEY)) {
			view.setScale(Integer.parseInt(descriptor.get(IMAGE_WIDGET_SCALE_KEY)));
		}
		if (descriptor.containsKey(IMAGE_WIDGET_ALT_TEXT_KEY)) {
			view.setAltText(descriptor.get(IMAGE_WIDGET_ALT_TEXT_KEY));
		}
	}

	public void configureWithoutUpload(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		configure(wikiKey, widgetDescriptor, dialogCallback);
		view.setWikiFilesTabVisible(false);
		if (!descriptor.containsKey(IMAGE_WIDGET_SYNAPSE_ID_KEY)) {
			view.showExternalTab();
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
	public void updateDescriptorFromView() {
		descriptor.clear();
		view.checkParams();
		if (!view.isExternal()) {
			if (view.isSynapseEntity()) {
				descriptor.put(IMAGE_WIDGET_SYNAPSE_ID_KEY, view.getSynapseId());
				Long version = view.getVersion();
				if (version != null) {
					descriptor.put(WIDGET_ENTITY_VERSION_KEY, version.toString());
				}
			} else if (!fileHandleIds.isEmpty()) {
				descriptor.put(IMAGE_WIDGET_FILE_NAME_KEY, file.getFileMeta().getFileName());
			} else {
				if (!wikiAttachments.isValid()) {
					throw new IllegalArgumentException(DisplayConstants.ERROR_SELECT_ATTACHMENT_MESSAGE);
				}
				descriptor.put(IMAGE_WIDGET_FILE_NAME_KEY, wikiAttachments.getSelectedFilename());
			}

			descriptor.put(ALIGNMENT_KEY, view.getAlignment());
			descriptor.put(IMAGE_WIDGET_SCALE_KEY, view.getScale().toString());
			descriptor.put(IMAGE_WIDGET_RESPONSIVE_KEY, Boolean.TRUE.toString());
			descriptor.put(IMAGE_WIDGET_ALT_TEXT_KEY, view.getAltText());
		}
	}

	@Override
	public String getTextToInsert() {
		if (view.isExternal())
			return "![" + view.getExternalAltText() + "](" + view.getImageUrl() + ")";
		else
			return null;
	}

	@Override
	public List<String> getDeletedFileHandleIds() {
		return wikiAttachments.getFilesHandlesToDelete();
	}

	@Override
	public List<String> getNewFileHandleIds() {
		return fileHandleIds;
	}

	/*
	 * Private Methods
	 */
}
