package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SynapseAPICallConfigEditor implements SynapseAPICallConfigView.Presenter, WidgetEditorPresenter {
	
	private SynapseAPICallConfigView view;
	private Map<String, String> descriptor;
	@Inject
	public SynapseAPICallConfigEditor(SynapseAPICallConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	@Override
	public void configure(String entityId, Map<String, String> widgetDescriptor) {
		descriptor = widgetDescriptor;
		String uri = descriptor.get(WidgetConstants.API_TABLE_WIDGET_PATH_KEY);
		if (uri != null)
			view.setApiUrl(uri);
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
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, view.getApiUrl());
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_COLUMNS_KEY, view.getColumnsToDisplay());
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_DISPLAY_COLUMN_NAMES_KEY, view.getColumnsToDisplay());
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_RENDERERS_KEY, view.getRendererNames());
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
