package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.presenter.EntityPresenter;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProjectBackgroundConfigEditor implements AttachmentConfigView.Presenter, WidgetEditorPresenter {
	
	private AttachmentConfigView view;
	private List<String> fileHandleIds;

	@Inject
	public ProjectBackgroundConfigEditor(AttachmentConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		fileHandleIds = new ArrayList<String>();
		view.configure(wikiKey, dialogCallback);
		view.showNote("<blockquote>File must be named <strong>" + EntityPresenter.ENTITY_BACKGROUND_IMAGE_NAME + "</strong></blockquote>");
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
	}
	
	@Override
	public String getTextToInsert() {
		return " ";
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
