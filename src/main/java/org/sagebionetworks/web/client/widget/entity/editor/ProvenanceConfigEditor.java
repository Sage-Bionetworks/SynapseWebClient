package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

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
	public void configure(String entityId, Map<String, String> widgetDescriptor) {
		descriptor = widgetDescriptor;
		String provEntityId = descriptor.get(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY);
		
		if (provEntityId != null)
			view.setEntityId(provEntityId);
		
		if (descriptor.get(WidgetConstants.PROV_WIDGET_DEPTH_KEY) != null){
			view.setDepth(Long.parseLong(descriptor.get(WidgetConstants.PROV_WIDGET_DEPTH_KEY)));
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
		descriptor.put(WidgetConstants.PROV_WIDGET_ENTITY_ID_KEY, view.getEntityId());
		//TODO: uncomment when the view sets these values
//		descriptor.put(WebConstants.PROV_WIDGET_DEPTH_KEY, view.getDepth().toString());
//		descriptor.put(WebConstants.PROV_WIDGET_EXPAND_KEY,Boolean.toString(view.isExpanded())); 
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
	
	/*
	 * Private Methods
	 */
}
