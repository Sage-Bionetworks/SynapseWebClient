package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class EntityBackgroundConfigEditor implements AttachmentConfigView.Presenter, WidgetEditorPresenter {
	
	private AttachmentConfigView view;
	private Map<String, String> descriptor;
	private List<String> fileHandleIds;

	@Inject
	public EntityBackgroundConfigEditor(AttachmentConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
		view.setAccept("image/png");
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		fileHandleIds = new ArrayList<String>();
		view.configure(wikiKey, dialogCallback);
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
