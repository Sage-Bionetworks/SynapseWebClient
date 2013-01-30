package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;

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
	private String rowNumberColName, jsonResultsArrayKeyName, cssStyleName, tableWidth, columnNames, displayColumnNames, rendererNames;
	
	@Inject
	public APITableWidget(APITableWidgetView view, SynapseClientAsync synapseClient, JSONObjectAdapter jsonObjectAdapter, PortalGinInjector ginInjector) {
		this.view = view;
		view.setPresenter(this);
		this.synapseClient = synapseClient;
		this.jsonObjectAdapter = jsonObjectAdapter;
		this.ginInjector = ginInjector;
	}
	
	@Override
	public void configure(String entityId, Map<String, String> widgetDescriptor) {
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
			
			columnNames = descriptor.get(WidgetConstants.API_TABLE_WIDGET_COLUMNS_KEY);
			displayColumnNames = descriptor.get(WidgetConstants.API_TABLE_WIDGET_DISPLAY_COLUMN_NAMES_KEY);
			rendererNames = descriptor.get(WidgetConstants.API_TABLE_WIDGET_RENDERERS_KEY);
			
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
										Object objValue = row.get(key);
										if (objValue != null)
											value = objValue.toString();
									}
									List<String> col = columnData.get(key);
									col.add(value);
								}
							}
							
							//define the column names
							String[] columnNamesArray = new String[]{};
							if (columnNames != null && columnNames.length() > 0) {
								columnNamesArray = columnNames.split(",");
							} else {
								int i = 0;
								columnNamesArray = new String[columnData.keySet().size()];
								for (Iterator<String> iterator = columnData.keySet().iterator(); iterator.hasNext();) {
									String columnName = iterator.next();
									columnNamesArray[i] = columnName;
									i++;
								}
							}
							String[] displayColumnNamesArray = columnNamesArray;
							if (displayColumnNames != null)
								displayColumnNamesArray = displayColumnNames.split(",");;
							String[] rendererNamesArray;
							if (rendererNames != null && rendererNames.length() > 0) {
								rendererNamesArray = rendererNames.split(",");
							} else {
								rendererNamesArray = new String[columnNamesArray.length];
								for (int i = 0; i < rendererNamesArray.length; i++) {
									rendererNamesArray[i] = WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE;
								}
							}
							APITableColumnRenderer[] renderers = new APITableColumnRenderer[rendererNamesArray.length];
							//we should not render until all of the column renderers have had a chance to initialize
							for (int i = 0; i < renderers.length; i++) {
								renderers[i] = createColumnRenderer(rendererNamesArray[i]);
							}
							testParameters(columnNamesArray, displayColumnNamesArray, renderers);
							APITableInitializedColumnRenderer[] initializedRenderers = new APITableInitializedColumnRenderer[rendererNamesArray.length];
							tableColumnRendererInit(columnData, columnNamesArray, displayColumnNamesArray, renderers, initializedRenderers, 0);
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
	
	private void testParameters(final String[] columnNames, final String[] displayColumnNames, final APITableColumnRenderer[] renderers) {
		//they must all be the same size
		int numberOfColumns = columnNames.length;
		if (displayColumnNames.length != numberOfColumns || renderers.length != numberOfColumns) {
			throw new IllegalArgumentException(DisplayConstants.API_TABLE_COLUMN_COUNT_MISMATCH);
		}
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
	private void tableColumnRendererInit(final Map<String, List<String>> columnData, final String[] columnNames, final String[] displayColumnNames, final APITableColumnRenderer[] renderers, final APITableInitializedColumnRenderer[] initializedRenderers, final int currentIndex) {
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
					view.configure(columnData, columnNames, displayColumnNames, initializedRenderers, tableWidth, isShowRowNumber, rowNumberColName, cssStyleName, offset);
					if (isPaging && total > pageSize) {
						int start = offset+1;
						int end = start + rowCount - 1;
						view.configurePager(start, end, total);
					}
				} else
					tableColumnRendererInit(columnData, columnNames, displayColumnNames, renderers, initializedRenderers, currentIndex+1);
			}
		};
		renderers[currentIndex].init(columnData.get(columnNames[currentIndex]), callback);
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

		/*
	 * Private Methods
	 */
}
