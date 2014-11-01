package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class AttachmentConfigEditor implements AttachmentConfigView.Presenter, WidgetEditorPresenter {
	
	private AttachmentConfigView view;
	private Map<String, String> descriptor;
	private List<String> fileHandleIds;
	@Inject
	public AttachmentConfigEditor(AttachmentConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Dialog window) {
		descriptor = widgetDescriptor;
		fileHandleIds = new ArrayList<String>();
		view.configure(wikiKey, window);
		try {
			//try to set the image widget file name
			if (descriptor.containsKey(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY)) {
				view.setUploadedFileHandleName(descriptor.get(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY));
				window.getButtonById(Dialog.OK).enable();
			}
		} catch (Exception e) {}
	}
	
	@SuppressWarnings("unchecked")
	public void clearState() {
		view.clear();
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void updateDescriptorFromView() {
		view.checkParams();
		descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, view.getUploadedFileHandleName());
	}
	
	@Override
	public String getTextToInsert() {
		return null;
	}

	@Override
	public int getDisplayHeight() {
		return view.getDisplayHeight();
	}
	@Override
	public int getAdditionalWidth() {
		return view.getAdditionalWidth();
	}
	
	@Override
	public void addFileHandleId(String fileHandleId) {
		fileHandleIds.add(fileHandleId);
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return fileHandleIds;
	}
	/*
	 * Private Methods
	 */
}
