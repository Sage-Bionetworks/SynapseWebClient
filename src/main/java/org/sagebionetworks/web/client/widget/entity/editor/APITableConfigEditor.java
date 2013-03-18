package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.Map;

import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableConfigEditor implements APITableConfigView.Presenter, WidgetEditorPresenter {
	
	private APITableConfigView view;
	private Map<String, String> descriptor;
	@Inject
	public APITableConfigEditor(APITableConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor) {
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
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, view.getApiUrl());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_WIDTH_KEY, view.getTableWidth());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, view.isPaging().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, view.getPageSize());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY, view.isShowRowNumbers().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY, view.getRowNumberColumnName());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY, view.getJsonResultsKeyName());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE, view.getCssStyle());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIGS, view.getColumnConfigs());
	}
	
	private void updateDescriptor(String key, String value) {
		if (value != null && value.trim().length() > 0)
			descriptor.put(key, value);
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
