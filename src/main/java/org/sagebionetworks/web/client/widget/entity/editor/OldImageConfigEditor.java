package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class OldImageConfigEditor implements OldImageConfigView.Presenter, WidgetEditorPresenter {
	
	private OldImageConfigView view;
	private Map<String, String> descriptor;
	
	@Inject
	public OldImageConfigEditor(OldImageConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		//TODO: change file upload to support other owner object types
		view.configure(wikiKey);
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
		if (!view.isExternal())
			descriptor.put(WidgetConstants.IMAGE_WIDGET_FILE_NAME_KEY, view.getUploadedAttachmentData().getName());
	}
	
	@Override
	public String getTextToInsert() {
		if (view.isExternal())
			return "!["+view.getAltText()+"]("+view.getImageUrl()+")";
		else return null;
	}

	
	/**
	 * widget creates entity AttachmentData objects, not file handles
	 */
	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}
	/*
	 * Private Methods
	 */
}
