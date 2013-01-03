package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetNameProvider;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class LinkConfigEditor implements LinkConfigView.Presenter, WidgetEditorPresenter {
	
	private LinkConfigView view;
	@Inject
	public LinkConfigEditor(LinkConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}		
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		//no way to edit an existing link
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
	public String getTextToInsert(String name) {
		return "["+name+"]("+view.getLinkUrl()+")";
	}
	
	@Override
	public void setNameProvider(WidgetNameProvider provider) {
	}
	
	/*
	 * Private Methods
	 */
}
