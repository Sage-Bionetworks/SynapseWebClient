package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ExternalImageConfigEditor implements ExternalImageConfigView.Presenter, WidgetEditorPresenter {
	
	private ExternalImageConfigView view;

	@Inject
	public ExternalImageConfigEditor(ExternalImageConfigView view) {
		this.view = view;
		view.setPresenter(this);
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, final DialogCallback dialogCallback) {
		view.initView();
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
		return "!["+view.getAltText()+"]("+view.getImageUrl()+")";
	}

	@Override
	public List<String> getNewFileHandleIds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<String> getDeletedFileHandleIds() {
		// TODO Auto-generated method stub
		return null;
	}
}
