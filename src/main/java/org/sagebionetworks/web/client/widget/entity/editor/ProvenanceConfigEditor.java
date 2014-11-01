package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.extjs.gxt.ui.client.widget.Dialog;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class ProvenanceConfigEditor implements ProvenanceConfigView.Presenter, WidgetEditorPresenter {
	
	private ProvenanceConfigView view;
	private Map<String, String> descriptor;
	@Inject
	public ProvenanceConfigEditor(ProvenanceConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}		

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Dialog window) {
		descriptor = widgetDescriptor;		
		if (descriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY) != null)
			view.setEntityList(descriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY));
		
		if (descriptor.get(WidgetConstants.PROV_WIDGET_DEPTH_KEY) != null){
			view.setDepth(Long.parseLong(descriptor.get(WidgetConstants.PROV_WIDGET_DEPTH_KEY)));
		}
		if (descriptor.get(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY) != null){
			view.setProvDisplayHeight(Integer.parseInt(descriptor.get(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY)));
		}
		if (descriptor.get(WidgetConstants.PROV_WIDGET_EXPAND_KEY) != null) {
			view.setIsExpanded(Boolean.parseBoolean(descriptor.get(WidgetConstants.PROV_WIDGET_EXPAND_KEY)));
		}
			
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
		descriptor.put(WidgetConstants.PROV_WIDGET_ENTITY_LIST_KEY, view.getEntityList());
		descriptor.put(WidgetConstants.PROV_WIDGET_DEPTH_KEY, view.getDepth().toString());
		if(view.getProvDisplayHeight() != null) descriptor.put(WidgetConstants.PROV_WIDGET_DISPLAY_HEIGHT_KEY, view.getProvDisplayHeight().toString());
		descriptor.put(WidgetConstants.PROV_WIDGET_EXPAND_KEY,Boolean.toString(view.isExpanded())); 
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
