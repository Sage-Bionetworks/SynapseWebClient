package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.extjs.gxt.ui.client.widget.Dialog;
public class QueryTableConfigEditor implements QueryTableConfigView.Presenter, WidgetEditorPresenter {
	
	private QueryTableConfigView view;
	private Map<String, String> descriptor;
	private static final String DEFAULT_PAGE_SIZE = "100";
	
	@Inject
	public QueryTableConfigEditor(QueryTableConfigView view) {
		this.view = view;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Dialog window) {
		descriptor = widgetDescriptor;
		APITableConfig tableConfig = new APITableConfig(widgetDescriptor);
		String uri = tableConfig.getUri();
		if (uri != null && uri.startsWith(ClientProperties.QUERY_SERVICE_PREFIX)) {
			//strip off prefix and decode query string
			uri = URL.decodeQueryString(uri.substring(ClientProperties.QUERY_SERVICE_PREFIX.length()));
			tableConfig.setUri(uri);
		}
		view.configure(tableConfig);
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
		descriptor.clear();
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, ClientProperties.QUERY_SERVICE_PREFIX + URL.encodeQueryString(view.getQueryString()));
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, view.isPaging().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, DEFAULT_PAGE_SIZE);
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY, view.isShowRowNumbers().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY, view.getRowNumberColumnName());
		List<APITableColumnConfig> configs = view.getConfigs();
		APITableConfigEditor.updateDescriptorWithColumnConfigs(descriptor, configs);
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
