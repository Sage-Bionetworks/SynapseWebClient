package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableWidget implements APITableWidgetView.Presenter, WidgetRendererPresenter {
	
	private APITableWidgetView view;
	private Map<String, String> descriptor;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private PortalGinInjector ginInjector;
	private int total, rowCount;
	private APITableConfig tableConfig;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;

	
	@Inject
	public APITableWidget(APITableWidgetView view, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, PortalGinInjector ginInjector,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.ginInjector = ginInjector;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor, Callback widgetRefreshRequired) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		tableConfig = new APITableConfig(descriptor);
		
		if (tableConfig.getUri() != null) {
			refreshData();
		}
		else
			view.showError(DisplayConstants.API_TABLE_MISSING_URI);
	}
	
	
	
	@Override
	public void pageBack() {
		tableConfig.setOffset(tableConfig.getOffset()-tableConfig.getPageSize());
		if (tableConfig.getOffset() < 0)
			tableConfig.setOffset(0);
		refreshData();
	}
	
	@Override
	public void pageForward() {
		tableConfig.setOffset(tableConfig.getOffset()+tableConfig.getPageSize());
		if (tableConfig.getOffset() > total)
			tableConfig.setOffset(total-tableConfig.getPageSize());
		refreshData();
	}
	
	
	private void refreshData() {
		String fullUri = tableConfig.getUri();
		
		if (tableConfig.isPaging()) {
			fullUri = getPagedURI();
		}
		synapseClient.getJSONEntity(fullUri, new AsyncCallback<String>() {
			@Override
			public void onSuccess(String result) {
				JSONObjectAdapter adapter;
				try {
					adapter = jsonObjectAdapter.createNew(result);
					if (adapter.has("totalNumberOfResults")) {
						total = adapter.getInt("totalNumberOfResults");
					}
					if (adapter.has(tableConfig.getJsonResultsArrayKeyName())) {
						JSONArrayAdapter resultsList = adapter.getJSONArray(tableConfig.getJsonResultsArrayKeyName());
						rowCount = resultsList.length();
						if (rowCount > 0) {
							JSONObjectAdapter firstItem = resultsList.getJSONObject(0);
							//initialize column data
							Map<String, List<String>> columnData = new HashMap<String, List<String>>();
							//initialize the column data lists
							for (Iterator<String> iterator = firstItem.keys(); iterator.hasNext();) {
								columnData.put(iterator.next(), new ArrayList<String>());
							}

							for (int i = 0; i < resultsList.length(); i++) {
								JSONObjectAdapter row = resultsList.getJSONObject(i);
								for (String key : columnData.keySet()) {
									String value = getColumnValue(row, key);
									List<String> col = columnData.get(key);
									col.add(value);
								}
							}
							
							//define the column names
							String[] columnNamesArray = new String[]{};
							columnNamesArray = new String[columnData.keySet().size()];
							int colNamesIndex = 0;
							for (Iterator<String> iterator = columnData.keySet().iterator(); iterator.hasNext();) {
								String columnName = iterator.next();
								columnNamesArray[colNamesIndex] = columnName;
								colNamesIndex++;
							}
							
							//if column configs were not passed in, then use default
							if (tableConfig.getColumnConfigs() == null || tableConfig.getColumnConfigs().size() == 0) {
								tableConfig.setColumnConfigs(getDefaultColumnConfigs(columnNamesArray));
							}
							
							APITableColumnRenderer[] renderers = new APITableColumnRenderer[tableConfig.getColumnConfigs().size()];
							int i = 0;
							for (APITableColumnConfig config : tableConfig.getColumnConfigs()) {
								renderers[i] = createColumnRendererInstance(ginInjector, config.getRendererFriendlyName());
								i++;
							}
							
							APITableInitializedColumnRenderer[] initializedRenderers = new APITableInitializedColumnRenderer[renderers.length];
							tableColumnRendererInit(columnData, columnNamesArray, renderers, initializedRenderers, 0);
						}
					}
				} catch (Exception e1) {
					onFailure(e1);
				}
			}
			
			
			@Override
			public void onFailure(Throwable caught) {
				view.showError(caught.getMessage());
			}
		});
	}
	
	private String getColumnValue(JSONObjectAdapter row, String key) throws JSONObjectAdapterException {
		String value = "";
		if (row.has(key)) {
			try {
				Object objValue = row.get(key);
				if (objValue != null)
					value = objValue.toString();
			} catch (JSONObjectAdapterException e) {
				//try to get it as an array
				JSONArrayAdapter valueArray = row.getJSONArray(key);
				StringBuilder valueArraySB = new StringBuilder();
				for (int j = 0; j < valueArray.length(); j++) {
					Object objValue = valueArray.get(j);
					if (objValue != null)
						valueArraySB.append(objValue.toString() + ",");
				}
				if (valueArraySB.length() > 0) {
					valueArraySB.deleteCharAt(valueArraySB.length()-1);
					value = valueArraySB.toString();
				}
			}
		}
		return value;
	}
	
	public String getPagedURI() {
		String uri = tableConfig.getUri();
		//special case for query service
		if (uri.startsWith(ClientProperties.QUERY_SERVICE_PREFIX)) {
			return tableConfig.getUri() + "+limit+"+tableConfig.getPageSize()+"+offset+"+(tableConfig.getOffset()+1);
		} else {
			String firstCharacter = tableConfig.getUri().contains("?") ? "&" : "?";
			return tableConfig.getUri() + firstCharacter + "limit="+tableConfig.getPageSize()+"&offset="+tableConfig.getOffset();	
		}
	}
	
	/**
	 * Recursive serially initializes the column renderers
	 * @param columnData
	 * @param columnNames
	 * @param displayColumnNames
	 * @param renderers
	 * @param currentIndex
	 */
	private void tableColumnRendererInit(final Map<String, List<String>> columnData, final String[] columnNames, final APITableColumnRenderer[] renderers, final APITableInitializedColumnRenderer[] initializedRenderers, final int currentIndex) {
		AsyncCallback<APITableInitializedColumnRenderer> callback = new AsyncCallback<APITableInitializedColumnRenderer>() {
			@Override
			public void onSuccess(APITableInitializedColumnRenderer result) {
				initializedRenderers[currentIndex] = result;
				processNext();
			}
			@Override
			public void onFailure(Throwable caught) {
				//there was a problem initializing a particular renderer
				view.showError(caught.getMessage());
			}
			private void processNext() {
				//after all renderers have initialized, then configure the view
				if (currentIndex == renderers.length-1) {
					view.configure(columnData, columnNames, initializedRenderers, tableConfig);
					if (tableConfig.isPaging() && total > tableConfig.getPageSize()) {
						int start = tableConfig.getOffset()+1;
						int end = start + rowCount - 1;
						view.configurePager(start, end, total);
					}
				} else
					tableColumnRendererInit(columnData, columnNames, renderers, initializedRenderers, currentIndex+1);
			}
		};
		APITableColumnConfig config = tableConfig.getColumnConfigs().get(currentIndex);
		renderers[currentIndex].init(columnData, config, callback);
	}
	
	private List<APITableColumnConfig> getDefaultColumnConfigs(String[] columnNamesArray) {
		List<APITableColumnConfig> defaultConfigs = new ArrayList<APITableColumnConfig>();
		//create a config for each column
		for (int i = 0; i < columnNamesArray.length; i++) {
			APITableColumnConfig newConfig = new APITableColumnConfig();
			newConfig.setDisplayColumnName(columnNamesArray[i]);
			Set<String> inputColumnSet = new HashSet<String>();
			inputColumnSet.add(columnNamesArray[i]);
			newConfig.setInputColumnNames(inputColumnSet);
			newConfig.setRendererFriendlyName(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE);
			defaultConfigs.add(newConfig);
		}
				
		return defaultConfigs;
	}
	
	/**
	 * Resolve a friendly renderer name (the name used in the editor and markdown) to a column renderer instance (initialize using injection).
	 * @param friendlyName
	 * @return
	 */
	public static APITableColumnRenderer createColumnRendererInstance(PortalGinInjector ginInjector, String friendlyName) {
		APITableColumnRenderer renderer;
		if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID))
			renderer = ginInjector.getAPITableColumnRendererUserId();
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_DATE))
			renderer = ginInjector.getAPITableColumnRendererDate();
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID))
			renderer = ginInjector.getAPITableColumnRendererSynapseID();
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_ANNOTATIONS))
			renderer = ginInjector.getAPITableColumnRendererEntityAnnotations();
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE))
			renderer = ginInjector.getAPITableColumnRendererNone();
		else
			throw new IllegalArgumentException("Unknown friendly column renderer name:" + friendlyName);
		
		return renderer;
	}
	
	
	@SuppressWarnings("unchecked")
	public void clearState() {
	}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public static List<String> wrap(String s) {
		List<String> colName = new ArrayList<String>();
		colName.add(s);
		return colName;
	}
	
	public static String getSingleOutputColumnName(APITableColumnConfig config) {
		String inputColumnName = getSingleInputColumnName(config);
		String outputColumnName = config.getDisplayColumnName();
		if (outputColumnName == null || outputColumnName.trim().length()==0)
			outputColumnName = inputColumnName;
		return outputColumnName;
	}
	
	public static String getSingleInputColumnName(APITableColumnConfig config) {
		if (config.getInputColumnNames() == null || config.getInputColumnNames().size() < 1) {
			throw new IllegalArgumentException("Must specific an input column name");
		}
		return config.getInputColumnNames().iterator().next();
	}
	
		/*
	 * Private Methods
	 */
}
