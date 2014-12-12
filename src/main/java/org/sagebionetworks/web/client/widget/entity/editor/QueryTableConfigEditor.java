package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
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
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
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
		String queryString = view.getQueryString();
		if (!DisplayUtils.isDefined(queryString)) {
			throw new IllegalArgumentException("A query is required.");
		}
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, ClientProperties.QUERY_SERVICE_PREFIX + URL.encodeQueryString(view.getQueryString()));
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, view.isPaging().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, DEFAULT_PAGE_SIZE);
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY, view.isShowRowNumbers().toString());
		List<APITableColumnConfig> configs = view.getConfigs();
		APITableConfigEditor.updateDescriptorWithColumnConfigs(descriptor, configs);
	}
	
	private void updateDescriptor(String key, String value) {
		if (value != null && value.trim().length() > 0)
			descriptor.put(key, value);
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
