package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sagebionetworks.repo.model.widget.APITableColumnConfig;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.transform.NodeModelCreator;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
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
	private String uri;
	private int offset, total, pageSize, rowCount;
	private Boolean isPaging,isShowRowNumber;
	private String rowNumberColName, jsonResultsArrayKeyName, cssStyleName, tableWidth;
	private List<APITableColumnConfig> columnConfigs;
	private NodeModelCreator nodeModelCreator;
	
	public final static String COLUMN_NAMES_DELIMITER = ";";	
	public final static String FIELD_DELIMITER = ",";	
	
	@Inject
	public APITableWidget(APITableWidgetView view, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, PortalGinInjector ginInjector, NodeModelCreator nodeModelCreator) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.ginInjector = ginInjector;
		this.nodeModelCreator = nodeModelCreator;
	}
	
	@Override
	public void configure(WikiPageKey wikiKey,
			Map<String, String> widgetDescriptor) {
		//set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		uri = descriptor.get(WidgetConstants.API_TABLE_WIDGET_PATH_KEY);
		if (uri != null) {
			isPaging = false;
			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY)){
				isPaging = Boolean.parseBoolean(descriptor.get(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY));
				if (isPaging) {
					//initialize the offset and pagesize
					offset=0;
					if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY))
						pageSize = Integer.parseInt(descriptor.get(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY));
					else
						pageSize = 10;
				}	
			}
			tableWidth = "";
			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_WIDTH_KEY)){
				tableWidth = descriptor.get(WidgetConstants.API_TABLE_WIDGET_WIDTH_KEY);
			}
			isShowRowNumber = false;
			rowNumberColName = "";
			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY)){
				isShowRowNumber = Boolean.parseBoolean(descriptor.get(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY));
				if (isShowRowNumber && descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY))
					rowNumberColName =descriptor.get(WidgetConstants.API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY);
			}
			
			jsonResultsArrayKeyName = "results";
			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY)){
				jsonResultsArrayKeyName = descriptor.get(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY);
			}
			
			cssStyleName = "";
			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE)){
				cssStyleName = descriptor.get(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE);
			}
			
			columnConfigs = new ArrayList<APITableColumnConfig>();
			//reconstruct table column configs (if there are any)
			int i = 0;
			while (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + i)) {
				String configString = descriptor.get(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX+i);
				String[] parts = configString.split(FIELD_DELIMITER);
				APITableColumnConfig config = new APITableColumnConfig();
				config.setRendererName(parts[0]);
				config.setDisplayColumnName(WidgetEncodingUtil.decodeValue(parts[1]));
				Set<String> inputColumnNames = new HashSet<String>();
				String[] inputColumns = parts[2].split(COLUMN_NAMES_DELIMITER);
				for (int j = 0; j < inputColumns.length; j++) {
					inputColumnNames.add(inputColumns[j]);
				}
				config.setInputColumnNames(inputColumnNames);
				
				columnConfigs.add(config);
				i++;
			}
			refreshData();
		}
		else
			view.showError(DisplayConstants.API_TABLE_MISSING_URI);
	}
	
	@Override
	public void pageBack() {
		offset = offset-pageSize;
		if (offset < 0)
			offset = 0;
		refreshData();
	}
	
	@Override
	public void pageForward() {
		offset = offset+pageSize;
		if (offset > total)
			offset = total-pageSize;
		refreshData();
	}
	
	
	private void refreshData() {
		String fullUri = uri;
		
		if (isPaging) {
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
					if (adapter.has(jsonResultsArrayKeyName)) {
						JSONArrayAdapter resultsList = adapter.getJSONArray(jsonResultsArrayKeyName);
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
							if (columnConfigs == null || columnConfigs.size() == 0) {
								columnConfigs = getDefaultColumnConfigs(columnNamesArray);
							}
							
							APITableColumnRenderer[] renderers = new APITableColumnRenderer[columnConfigs.size()];
							int i = 0;
							for (Iterator columnConfigIterator = columnConfigs.iterator(); columnConfigIterator
									.hasNext();) {
								APITableColumnConfig config = (APITableColumnConfig) columnConfigIterator.next();
								renderers[i] = createColumnRenderer(config.getRendererName());
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
				caught.printStackTrace();
				view.showError(caught.getMessage());
			}
		});
	}
	
	private String getPagedURI() {
		String firstCharacter = uri.contains("?") ? "&" : "?";
		return uri + firstCharacter + "limit="+pageSize+"&offset="+offset;
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
				//keep going
				processNext();
			}
			private void processNext() {
				//after all renderers have initialized, then configure the view
				if (currentIndex == renderers.length-1) {
					view.configure(columnData, columnNames, initializedRenderers, tableWidth, isShowRowNumber, rowNumberColName, cssStyleName, offset);
					if (isPaging && total > pageSize) {
						int start = offset+1;
						int end = start + rowCount - 1;
						view.configurePager(start, end, total);
					}
				} else
					tableColumnRendererInit(columnData, columnNames, renderers, initializedRenderers, currentIndex+1);
			}
		};
		APITableColumnConfig config = columnConfigs.get(currentIndex);
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
			newConfig.setRendererName(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE);
			defaultConfigs.add(newConfig);
		}
				
		return defaultConfigs;
	}
	
	public APITableColumnRenderer createColumnRenderer(String rendererName) {
		APITableColumnRenderer renderer;
		if (rendererName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID))
			renderer = ginInjector.getAPITableColumnRendererUserId();
		else if (rendererName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_DATE))
			renderer = ginInjector.getAPITableColumnRendererDate();
		else if (rendererName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID))
			renderer = ginInjector.getAPITableColumnRendererSynapseID();
		else if (rendererName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_ANNOTATIONS))
			renderer = ginInjector.getAPITableColumnRendererEntityAnnotations();
		else
			renderer = ginInjector.getAPITableColumnRendererNone();
		
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
