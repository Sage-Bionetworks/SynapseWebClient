package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.query.QueryTableResults;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
public class QueryTableConfigEditor implements QueryTableConfigView.Presenter, WidgetEditorPresenter {
	
	private QueryTableConfigView view;
	private Map<String, String> descriptor;
	public static final String DEFAULT_PAGE_SIZE = "100";
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private String servicePrefix;
	private GWTWrapper gwt;
	
	@Inject
	public QueryTableConfigEditor(QueryTableConfigView view, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, GWTWrapper gwt) {
		this.view = view;
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.gwt = gwt;
		view.setPresenter(this);
		view.initView();
		servicePrefix = ClientProperties.QUERY_SERVICE_PREFIX;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		APITableConfig tableConfig = new APITableConfig(widgetDescriptor);
		String uri = tableConfig.getUri();
		if (uri != null) {
			//strip off prefix and decode query string
			if (uri.startsWith(servicePrefix)) {
				uri = gwt.decodeQueryString(uri.substring(servicePrefix.length()));
			} 
			tableConfig.setUri(uri);
		} 
		view.configure(tableConfig);
	}
	
	public void setServicePrefix(String servicePrefix) {
		this.servicePrefix = servicePrefix;
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
		List<APITableColumnConfig> configs = view.getConfigs();
		APITableConfigEditor.updateDescriptorWithColumnConfigs(descriptor, configs);
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

					updateDescriptorFromView();
					APITableConfig tableConfig = new APITableConfig(descriptor);
					///////////////////
					List<String> headers = new ArrayList<String>();
					
					if (tableConfig.isQueryTableResults()) {
						QueryTableResults results = new QueryTableResults();
						results.initializeFromJSONObject(adapter);
						headers = results.getHeaders();
					}
					else if (adapter.has(tableConfig.getJsonResultsArrayKeyName())) {
						JSONArrayAdapter resultsList = adapter.getJSONArray(tableConfig.getJsonResultsArrayKeyName());
						int rowCount = resultsList.length();
						if (rowCount > 0) {
							JSONObjectAdapter firstItem = resultsList.getJSONObject(0);
							for (Iterator iterator = firstItem.keys(); iterator.hasNext();) {
								headers.add((String) iterator.next());
							}
						}
					}
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
	
	private String getServicePathFromView() {
		return servicePrefix + gwt.encodeQueryString(view.getQueryString());
	}
	

	public String getQueryString() {
		return view.getQueryString();
	}
	/*
	 * Private Methods
	 */
}
