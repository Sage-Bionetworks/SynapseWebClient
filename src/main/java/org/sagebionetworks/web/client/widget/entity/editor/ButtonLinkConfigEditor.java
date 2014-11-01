package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ButtonLinkConfigEditor implements ButtonLinkConfigView.Presenter, WidgetEditorPresenter {
	private Map<String, String> descriptor;
	private ButtonLinkConfigView view;
	
	@Inject
	public ButtonLinkConfigEditor(ButtonLinkConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}		
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Dialog window) {
		descriptor = widgetDescriptor;
		view.configure(wikiKey, widgetDescriptor);
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
		//update widget descriptor from the view
		view.checkParams();
		descriptor.put(WidgetConstants.TEXT_KEY, view.getName());
		descriptor.put(WidgetConstants.LINK_URL_KEY, view.getLinkUrl());
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
	public String getTextToInsert() {
		return null;
	}
	
	@Override
	public List<String> getNewFileHandleIds() {
		return null;
	}
	
	/*
	 * Private Methods
	 */
}
