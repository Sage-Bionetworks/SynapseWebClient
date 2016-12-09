package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.repo.model.query.QueryTableResults;
import org.sagebionetworks.repo.model.query.Row;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.ElementWrapper;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.TableUnavilableException;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableWidget implements APITableWidgetView.Presenter, WidgetRendererPresenter {
	
	public static final String CURRENT_USER_SQL_VARIABLE = "@CURRENT_USER";
	public static final String ENCODED_CURRENT_USER_SQL_VARIABLE = "%40CURRENT_USER";
	private APITableWidgetView view;
	private Map<String, String> descriptor;
	private SynapseClientAsync synapseClient;
	private JSONObjectAdapter jsonObjectAdapter;
	private PortalGinInjector ginInjector;
	private int total, rowCount;
	private APITableConfig tableConfig;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	SynapseAlert synAlert;
	private GWTWrapper gwt;
	
	public static Set<String> userColumnNames = new HashSet<String>();
	public static Set<String> dateColumnNames = new HashSet<String>();
	public static Set<String> synapseIdColumnNames = new HashSet<String>();
	static {
		userColumnNames.add(WebConstants.DEFAULT_COL_NAME_CREATED_BY_PRINCIPAL_ID);
		userColumnNames.add(WebConstants.DEFAULT_COL_NAME_MODIFIED_BY_PRINCIPAL_ID);
		userColumnNames.add(WebConstants.DEFAULT_COL_NAME_USER_ID);

		dateColumnNames.add(WebConstants.DEFAULT_COL_NAME_CREATED_ON);
		dateColumnNames.add(WebConstants.DEFAULT_COL_NAME_MODIFIED_ON);
		
		synapseIdColumnNames.add(WebConstants.DEFAULT_COL_NAME_ENTITY_ID);
		synapseIdColumnNames.add(WebConstants.DEFAULT_COL_NAME_PARENT_ID);
	}
	
	@Inject
	public APITableWidget(APITableWidgetView view, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, PortalGinInjector ginInjector,
			GlobalApplicationState globalApplicationState,
			AuthenticationController authenticationController,
			SynapseAlert synAlert, GWTWrapper gwt) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.ginInjector = ginInjector;
		this.synAlert = synAlert;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.gwt = gwt;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		view.clear();
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		tableConfig = new APITableConfig(descriptor);
		
		//if the table is configured to only show if the user is logged in, and we are not logged in, then just return.
		if (!authenticationController.isLoggedIn() && tableConfig.isShowOnlyIfLoggedIn())
			return;
		
		if (tableConfig.getUri() != null) {
			refreshData();
		}
		else {
			synAlert.showError(DisplayConstants.API_TABLE_MISSING_URI);
			view.showError(synAlert.asWidget());
		}
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
	
	@Override
	public void refreshData() {
		String fullUri = tableConfig.getUri();
		
		if (tableConfig.isPaging()) {
			//SWC-1133: only modify the URI with Ordered By if we are paging (otherwise, JQuery tablesorter will handle sorting)
			fullUri = getOrderedByURI(fullUri, tableConfig);
			fullUri = getPagedURI(fullUri);
		}
		
		
		if (authenticationController.isLoggedIn()) {
			String userId = authenticationController.getCurrentUserPrincipalId();
			fullUri = fullUri.replace(CURRENT_USER_SQL_VARIABLE, userId).replace(ENCODED_CURRENT_USER_SQL_VARIABLE, userId);
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
					Map<String, List<String>> columnData = null;
					if (tableConfig.isQueryTableResults()) {
						//initialize
						QueryTableResults results = new QueryTableResults();
						results.initializeFromJSONObject(adapter);
						rowCount = results.getRows().size();
						if (rowCount > 0) {
							//initialize column data
							columnData = createColumnDataMap(results.getHeaders().iterator());
							//quick lookup for column index
							Map<Integer, String> columnIndexMap = new HashMap<Integer, String>();
							for (int i = 0; i < results.getHeaders().size(); i++) {
								columnIndexMap.put(i, results.getHeaders().get(i));
							}
							//transform results into column data
							for (Row row : results.getRows()) {
								//add values to the appropriate column lists
								for (int i = 0; i < row.getValues().size(); i++) {
									List<String> col = columnData.get(columnIndexMap.get(i));
									col.add(row.getValues().get(i));
								}
							}
						}
					}
					else if (adapter.has(tableConfig.getJsonResultsArrayKeyName())) {
						JSONArrayAdapter resultsList = adapter.getJSONArray(tableConfig.getJsonResultsArrayKeyName());
						rowCount = resultsList.length();
						if (rowCount > 0) {
							JSONObjectAdapter firstItem = resultsList.getJSONObject(0);
							//initialize column data
							columnData = createColumnDataMap(firstItem.keys());
							
							//transform results into column data
							for (int i = 0; i < resultsList.length(); i++) {
								JSONObjectAdapter row = resultsList.getJSONObject(i);
								for (String key : columnData.keySet()) {
									String value = getColumnValue(row, key);
									List<String> col = columnData.get(key);
									col.add(value);
								}
							}
						}
					}
					
					if (columnData != null) {
						//if node query, remove the object type from the column names (ie remove "project." from "project.id")
						if(isNodeQueryService(tableConfig.getUri())) {
							fixColumnNames(columnData);
						}
						//define the column names
						String[] columnNamesArray = getColumnNamesArray(columnData.keySet());
						//create renderers
						APITableColumnRenderer[] renderers = createRenderers(columnNamesArray, tableConfig, ginInjector);
						APITableInitializedColumnRenderer[] initializedRenderers = new APITableInitializedColumnRenderer[renderers.length];
						tableColumnRendererInit(columnData, columnNamesArray, renderers, initializedRenderers, 0);
					}
					
				} catch (Exception e1) {
					onFailure(e1);
				}
			}
			
			@Override
			public void onFailure(Throwable caught) {
				if(caught instanceof TableUnavilableException)
					view.showTableUnavailable();
				else {
					synAlert.handleException(caught);
					view.showError(synAlert.asWidget());
				}
			}
		});
	}
	
	public static void fixColumnNames(Map<String, List<String>> columnData) {
		Set<String> initialKeySet = new LinkedHashSet<String>();
		initialKeySet.addAll(columnData.keySet());
		for (String key : initialKeySet) {
			List<String> columnValues = columnData.remove(key);
			columnData.put(removeFirstToken(key), columnValues);
		}
	}
	
	public static String removeFirstToken(String colName) {
		if (colName == null)
			return colName;
		int dotIndex = colName.indexOf('.');
		if (dotIndex > -1)
			return colName.substring(dotIndex+1);
		else return colName;
	}
	
	public Map<String, List<String>> createColumnDataMap(Iterator<String> iterator) {
		//initialize column data
		Map<String, List<String>> columnData = new LinkedHashMap<String, List<String>>();
		if (iterator != null) {
			//initialize the column data lists
			for (; iterator.hasNext();) {
				columnData.put(iterator.next(), new ArrayList<String>());
			}
		}
		return columnData;
	}
	
	public APITableColumnRenderer[] createRenderers(String[] columnNamesArray, APITableConfig tableConfig, PortalGinInjector ginInjector) {
		//if column configs were not passed in, then use default
		if (tableConfig.getColumnConfigs() == null || tableConfig.getColumnConfigs().size() == 0) {
			tableConfig.setColumnConfigs(getDefaultColumnConfigs(columnNamesArray, tableConfig));
		}
		
		APITableColumnRenderer[] renderers = new APITableColumnRenderer[tableConfig.getColumnConfigs().size()];
		int i = 0;
		for (APITableColumnConfig config : tableConfig.getColumnConfigs()) {
			renderers[i] = createColumnRendererInstance(ginInjector, config.getRendererFriendlyName());
			i++;
		}
		
		return renderers;
	}
	
	public String[] getColumnNamesArray(Set<String> columnNames){
		String[] columnNamesArray = new String[]{};
		if (columnNames != null) {
			columnNamesArray = new String[columnNames.size()];
			int colNamesIndex = 0;
			for (String colName : columnNames) {
				columnNamesArray[colNamesIndex] = colName;
				colNamesIndex++;
			}
		}
		return columnNamesArray;
	}
	
	public static String getColumnValue(JSONObjectAdapter row, String key) throws JSONObjectAdapterException {
		String value = "";
		if (row.has(key)) {
			try {
				Object objValue;
				//try to parse it as a String, then as a Long, then fall back to get object
				try {
					objValue = row.getString(key);
				} catch (JSONObjectAdapterException e) {
					try {
						objValue = row.getLong(key);
					} catch (JSONObjectAdapterException e1) {
						objValue = row.get(key);
					}
				}
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
	
	public String getPagedURI(String uri) {
		//special case for query service
		boolean isSubmissionQueryService = isSubmissionQueryService(uri);
		boolean isNodeQueryService = isNodeQueryService(uri);
		if (isSubmissionQueryService || isNodeQueryService) {
			//the node query service's first element is at index 1! (submission query service first element is at index 0)
			Long firstIndex = isSubmissionQueryService ? ServiceConstants.DEFAULT_PAGINATION_OFFSET : ServiceConstants.DEFAULT_PAGINATION_OFFSET_NO_OFFSET_EQUALS_ONE;
			return uri + "+limit+"+tableConfig.getPageSize()+"+offset+"+(tableConfig.getOffset()+firstIndex);
		} else {
			String firstCharacter = uri.contains("?") ? "&" : "?";
			return uri + firstCharacter + "limit="+tableConfig.getPageSize()+"&offset="+tableConfig.getOffset();	
		}
	}
	
	public String getOrderedByURI(String uri, APITableConfig tableConfig) {
		String newUri = uri;
		if (isQueryService(uri)) {
			//find the order by column
			COLUMN_SORT_TYPE sort = COLUMN_SORT_TYPE.NONE;
			String columnName = null;
			for (APITableColumnConfig columnConfig : tableConfig.getColumnConfigs()) {
				if (columnConfig.getSort() != null && COLUMN_SORT_TYPE.NONE != columnConfig.getSort()) {
					//found
					Set<String> inputColumnNames = columnConfig.getInputColumnNames();
					if (inputColumnNames.size() > 0) {
						//take one
						columnName = inputColumnNames.iterator().next();
						sort = columnConfig.getSort();
						break;
					}
				}
			}
			int orderByIndex = newUri.toLowerCase().indexOf("+order+by+");
			if (orderByIndex != -1) {
				newUri = newUri.substring(0, orderByIndex);
			}
			//if there is something to sort
			if (COLUMN_SORT_TYPE.NONE != sort) {
				newUri = newUri + "+order+by+%22"+columnName+"%22+"+sort.toString();
			}
		}
		return newUri;
	}
	
	public static boolean isQueryService(String uri) {
		return isNodeQueryService(uri) || isSubmissionQueryService(uri);
	}
	
	public static boolean isNodeQueryService(String uri) {
		return uri.startsWith(ClientProperties.QUERY_SERVICE_PREFIX);
	}
	
	public static boolean isSubmissionQueryService(String uri) {
		return uri.startsWith(ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX);
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
				synAlert.handleException(caught);
				view.showError(synAlert.asWidget());
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
					// fill in user badges and cancel buttons
					injectWidgets();
				} else
					tableColumnRendererInit(columnData, columnNames, renderers, initializedRenderers, currentIndex+1);
			}
		};

		APITableColumnConfig config = tableConfig.getColumnConfigs().get(currentIndex);
		renderers[currentIndex].init(columnData, config, callback);
	}
	
	public void injectWidgets() {
		List<ElementWrapper> divs = view.findCancelRequestDivs();
		for (ElementWrapper div : divs) {
			div.removeAllChildren();
			String json = gwt.decodeQueryString(div.getAttribute("value"));
			CancelControlWidget cancelRequestWidget = ginInjector.getCancelControlWidget();
			cancelRequestWidget.configure(json);
			view.addWidget(cancelRequestWidget.asWidget(), div.getAttribute("id"));
		}
		divs = view.findUserBadgeDivs();
		for (ElementWrapper div : divs) {
			div.removeAllChildren();
			String userId = div.getAttribute("value");
			UserBadge userBadge = ginInjector.getUserBadgeWidget();
			userBadge.configure(userId);
			view.addWidget(userBadge.asWidget(), div.getAttribute("id"));
		}
	}
	
	/**
	 * The renderers are built directly from the table column configs.  The view will tell us when column from this renderer was clicked by the user.
	 * 
	 * @param index
	 */
	@Override
	public void columnConfigClicked(APITableColumnConfig columnConfig) {
		//usually handled by JQuery tablesorter plugin, but if this is a query service (evaluation or regular query) then we should append to the uri an appropriate order by
		if (isQueryService(tableConfig.getUri())) {
			//set all column sort values
			for (APITableColumnConfig config : tableConfig.getColumnConfigs()) {
				COLUMN_SORT_TYPE sort = COLUMN_SORT_TYPE.NONE;
				if (columnConfig == config) {
					//flip to ASC if already DESC
					sort = COLUMN_SORT_TYPE.DESC == config.getSort() ? COLUMN_SORT_TYPE.ASC : COLUMN_SORT_TYPE.DESC;
				}
				config.setSort(sort);
			}
			//then refresh the data
			refreshData();
		}
	}
	
	private List<APITableColumnConfig> getDefaultColumnConfigs(String[] columnNamesArray, APITableConfig tableConfig) {
		List<APITableColumnConfig> defaultConfigs = new ArrayList<APITableColumnConfig>();
		//create a config for each column
		for (int i = 0; i < columnNamesArray.length; i++) {
			APITableColumnConfig newConfig = new APITableColumnConfig();
			String currentColumnName = columnNamesArray[i];
			String displayColumnName = currentColumnName;
			newConfig.setDisplayColumnName(displayColumnName);
			Set<String> inputColumnSet = new HashSet<String>();
			inputColumnSet.add(currentColumnName);
			newConfig.setInputColumnNames(inputColumnSet);
			newConfig.setRendererFriendlyName(guessRendererFriendlyName(displayColumnName, tableConfig));
			defaultConfigs.add(newConfig);
		}
				
		return defaultConfigs;
	}
	
	/**
	 * make a best guess as to what the renderer type should be
	 * @param columnName
	 * @return
	 */
	public String guessRendererFriendlyName(String columnName, APITableConfig tableConfig) {
		String defaultRendererName =  WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE;
		if (columnName != null) {
			String lowerCaseColumnName = columnName.toLowerCase();
			if (userColumnNames.contains(lowerCaseColumnName)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID;
			} else if (dateColumnNames.contains(lowerCaseColumnName)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE;
			} else if (synapseIdColumnNames.contains(lowerCaseColumnName) || 
					(isNodeQueryService(tableConfig.getUri()) && WebConstants.DEFAULT_COL_NAME_ID.equals(lowerCaseColumnName))) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID;
			} else if (lowerCaseColumnName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_CANCEL_CONTROL)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_CANCEL_CONTROL;
			}
		}
		return defaultRendererName;
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
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_DATE) || friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE))
			renderer = ginInjector.getAPITableColumnRendererDate();
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_LINK))
			renderer = ginInjector.getAPITableColumnRendererLink();
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID))
			renderer = ginInjector.getAPITableColumnRendererSynapseID();
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_ANNOTATIONS))
			renderer = ginInjector.getAPITableColumnRendererEntityAnnotations();
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_CANCEL_CONTROL))
			renderer = ginInjector.getAPITableColumnRendererCancelControl();
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
	
	public static List<String> getColumnValues(String inputColumnName, Map<String, List<String>> columnData) {
		List<String> colValues = columnData.get(inputColumnName);
		if (colValues == null) {
			//try to find using the fixed column value
			colValues = columnData.get(removeFirstToken(inputColumnName));
		}
		if (colValues == null) {
			//column not found in the data.  return an empty column
			if (!columnData.values().isEmpty()) {
				colValues = new ArrayList<String>();
				List<String> aColumn = columnData.values().iterator().next();
				for (int i = 0; i < aColumn.size(); i++) {
					colValues.add(null);
				}
			}
		}
		return colValues;
	}
	
	// for testing only
	public void setTableConfig(APITableConfig tableConfig) {
		this.tableConfig = tableConfig;
	}
	
		/*
	 * Private Methods
	 */
}
