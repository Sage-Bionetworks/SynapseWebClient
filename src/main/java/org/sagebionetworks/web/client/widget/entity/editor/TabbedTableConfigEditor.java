package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.extjs.gxt.ui.client.widget.Dialog;
public class TabbedTableConfigEditor implements TabbedTableConfigView.Presenter, WidgetEditorPresenter {
	
	private TabbedTableConfigView view;
	@Inject
	public TabbedTableConfigEditor(TabbedTableConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}		
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Dialog window) {
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
	public String getTextToInsert() {
		//here is the main function of this editor.  replace all tabs with '|'
		String data = view.getTableContents();
		return data.replaceAll("\t", "|");
	}
	
	/*
	 * Private Methods
	 */
}
