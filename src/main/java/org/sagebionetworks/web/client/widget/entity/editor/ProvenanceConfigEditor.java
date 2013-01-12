package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.widget.ProvenanceWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ProvenanceConfigEditor implements ProvenanceConfigView.Presenter, WidgetEditorPresenter {
	
	private ProvenanceConfigView view;
	private ProvenanceWidgetDescriptor descriptor;
	@Inject
	public ProvenanceConfigEditor(ProvenanceConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}		
	@Override
	public void configure(String entityId, WidgetDescriptor widgetDescriptor) {
		if (!(widgetDescriptor instanceof ProvenanceWidgetDescriptor))
			throw new IllegalArgumentException(DisplayConstants.INVALID_WIDGET_DESCRIPTOR_TYPE);
		descriptor = (ProvenanceWidgetDescriptor) widgetDescriptor;
		String provEntityId = descriptor.getEntityId();
		
		if (provEntityId != null)
			view.setEntityId(provEntityId);
		if (descriptor.getDepth() != null){
			view.setDepth(Long.parseLong(descriptor.getDepth()));
		}
			
		if (descriptor.getShowExpand() != null) {
			view.setIsExpanded(Boolean.parseBoolean(descriptor.getShowExpand()));
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
		descriptor.setEntityId(view.getEntityId());
		//TODO: uncomment when the view sets these values
//		descriptor.setDepth(view.getDepth().toString());
//		descriptor.setShowExpand(Boolean.toString(view.isExpanded()));
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
