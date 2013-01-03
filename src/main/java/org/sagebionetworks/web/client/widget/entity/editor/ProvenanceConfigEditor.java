package org.sagebionetworks.web.client.widget.entity.editor;

import org.sagebionetworks.repo.model.widget.ProvenanceWidgetDescriptor;
import org.sagebionetworks.repo.model.widget.WidgetDescriptor;
import org.sagebionetworks.repo.model.widget.YouTubeWidgetDescriptor;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.WidgetNameProvider;

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
		Long depth = descriptor.getDepth();
		Boolean showExpand = descriptor.getShowExpand();
		String provEntityId = descriptor.getEntityId();
		
		if (provEntityId != null)
			view.setEntityId(provEntityId);
		if (depth != null)
			view.setDepth(depth);
		if (showExpand != null)
			view.setIsExpanded(showExpand);
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
		descriptor.setDepth(view.getDepth());
		descriptor.setShowExpand(view.isExpanded());
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
		return null;
	}
	
	@Override
	public void setNameProvider(WidgetNameProvider provider) {
	}
	
	/*
	 * Private Methods
	 */
}
