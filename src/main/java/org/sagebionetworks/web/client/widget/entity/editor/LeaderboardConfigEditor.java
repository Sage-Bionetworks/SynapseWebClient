package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.query.QueryTableResults;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class LeaderboardConfigEditor implements LeaderboardConfigView.Presenter, WidgetEditorPresenter {
	
	private LeaderboardConfigView view;
	private Map<String, String> descriptor;
	private static final String DEFAULT_PAGE_SIZE = "100";
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	
	@Inject
	public LeaderboardConfigEditor(LeaderboardConfigView view, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		view.setPresenter(this);
		view.initView();
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		APITableConfig tableConfig = new APITableConfig(widgetDescriptor);
		String uri = tableConfig.getUri();
		if (uri != null && uri.startsWith(ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX)) {
			//strip off prefix and decode query string
			uri = URL.decodeQueryString(uri.substring(ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX.length()));
			tableConfig.setUri(uri);
		}
		view.configure(tableConfig);
	}
	
	@Override
	public void autoAddColumns() {
		// execute the current query to get the column headers, then update the column manager
		String path = getServicePathFromView() + "+limit+10";
		synapseClient.getJSONEntity(path, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				JSONObjectAdapter adapter;
				try {
					adapter = jsonObjectAdapter.createNew(result);
					//initialize
					QueryTableResults results = new QueryTableResults();
					results.initializeFromJSONObject(adapter);
					List<String> headers = results.getHeaders();
					List<APITableColumnConfig> newConfigs = new ArrayList<APITableColumnConfig>();
					for (String headerName : headers) {
						APITableColumnConfig config = new APITableColumnConfig();
						HashSet<String> inputColumnName = new HashSet<String>();
						inputColumnName.add(headerName);
						config.setInputColumnNames(inputColumnName);
						config.setRendererFriendlyName(guessRendererFriendlyName(headerName));
						newConfigs.add(config);
					}
					view.setConfigs(newConfigs);
					
				} catch (Exception e1) {
					onFailure(e1);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.showErrorMessage(caught.getMessage());
			}
		});
	}
	
	/**
	 * make a best guess as to what the renderer type should be
	 * @param columnName
	 * @return
	 */
	public String guessRendererFriendlyName(String columnName) {
		String defaultRendererName =  WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE;
		if (columnName != null) {
			String lowerCaseColumnName = columnName.toLowerCase();
			if (APITableWidget.userColumnNames.contains(lowerCaseColumnName)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID;
			} else if (APITableWidget.dateColumnNames.contains(lowerCaseColumnName)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE;
			} else if (APITableWidget.synapseIdColumnNames.contains(lowerCaseColumnName)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID;
			}
		}
		return defaultRendererName;
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
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, getServicePathFromView());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, view.isPaging().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, DEFAULT_PAGE_SIZE);
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY, view.isShowRowNumbers().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY, "rows");
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_QUERY_TABLE_RESULTS, Boolean.TRUE.toString());
		List<APITableColumnConfig> configs = view.getConfigs();
		APITableConfigEditor.updateDescriptorWithColumnConfigs(descriptor, configs);
	}
	
	private String getServicePathFromView() {
		return ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX + URL.encodeQueryString(view.getQueryString());
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
	@Override
	public List<String> getDeletedFileHandleIds() {
		return null;
	}
	/*
	 * Private Methods
	 */
}
