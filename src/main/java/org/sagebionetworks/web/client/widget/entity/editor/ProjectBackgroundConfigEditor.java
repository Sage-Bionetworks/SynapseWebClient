package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.widget.entity.WikiAttachments;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.upload.FileHandleUploadWidget;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.inject.Inject;

public class ProjectBackgroundConfigEditor extends AttachmentConfigEditor {
	private AttachmentConfigView view;
	
	@Inject
	public ProjectBackgroundConfigEditor(AttachmentConfigView view, FileHandleUploadWidget fileInputWidget, WikiAttachments wikiAttachments) {
		super(view, fileInputWidget, wikiAttachments);
		this.view = view;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		super.configure(wikiKey, widgetDescriptor, dialogCallback);
		
	}
	
	@Override
	public String getTextToInsert() {
		return " ";
	}
}
