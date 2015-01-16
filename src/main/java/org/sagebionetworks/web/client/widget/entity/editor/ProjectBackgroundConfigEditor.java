package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.upload.FileInputWidget;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.inject.Inject;

public class ProjectBackgroundConfigEditor extends AttachmentConfigEditor {
	private AttachmentConfigView view;
	
	@Inject
	public ProjectBackgroundConfigEditor(AttachmentConfigView view, FileInputWidget fileInputWidget) {
		super(view, fileInputWidget);
		this.view = view;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		super.configure(wikiKey, widgetDescriptor, dialogCallback);
		view.showNote("<blockquote>File must be named <strong>" + EntityPresenter.ENTITY_BACKGROUND_IMAGE_NAME + "</strong></blockquote>");
	}
	
	@Override
	public String getTextToInsert() {
		return " ";
	}
}
