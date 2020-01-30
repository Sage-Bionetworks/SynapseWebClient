package org.sagebionetworks.web.client.widget.table.api;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
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
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DivView;
import org.sagebionetworks.web.client.widget.WidgetRendererPresenter;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.entity.renderer.CancelControlWidget;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.TableUnavilableException;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableWidget implements APITableWidgetView.Presenter, WidgetRendererPresenter {

	public static final String CURRENT_USER_SQL_VARIABLE = "@CURRENT_USER";
	public static final String ENCODED_CURRENT_USER_SQL_VARIABLE = "%40CURRENT_USER";
	private APITableWidgetView view;
	private Map<String, String> descriptor;
	private SynapseJavascriptClient jsClient;
	private PortalGinInjector ginInjector;
	private int total, rowCount;
	private APITableConfig tableConfig;
	GlobalApplicationState globalApplicationState;
	AuthenticationController authenticationController;
	SynapseAlert synAlert;
	public CellFactory cellFactory;

	private static final RegExp WORD_PATTERN = RegExp.compile("^\\w*$");

	public static Set<String> userColumnNames = new HashSet<String>();
	public static Set<String> dateColumnNames = new HashSet<String>();
	public static Set<String> synapseIdColumnNames = new HashSet<String>();
	static {
		userColumnNames.add(WebConstants.DEFAULT_COL_NAME_CREATED_BY_PRINCIPAL_ID);
		userColumnNames.add(WebConstants.DEFAULT_COL_NAME_MODIFIED_BY_PRINCIPAL_ID);
		userColumnNames.add(WebConstants.DEFAULT_COL_NAME_USER_ID);
		userColumnNames.add(WebConstants.DEFAULT_COL_NAME_SUBMITTER_ID);

		dateColumnNames.add(WebConstants.DEFAULT_COL_NAME_CREATED_ON);
		dateColumnNames.add(WebConstants.DEFAULT_COL_NAME_MODIFIED_ON);

		synapseIdColumnNames.add(WebConstants.DEFAULT_COL_NAME_ENTITY_ID);
		synapseIdColumnNames.add(WebConstants.DEFAULT_COL_NAME_PARENT_ID);
	}

	@Inject
	public APITableWidget(APITableWidgetView view, SynapseJavascriptClient jsClient, PortalGinInjector ginInjector, GlobalApplicationState globalApplicationState, AuthenticationController authenticationController, SynapseAlert synAlert, CellFactory cellFactory) {
		this.view = view;
		view.setPresenter(this);
		this.jsClient = jsClient;
		this.ginInjector = ginInjector;
		this.synAlert = synAlert;
		this.globalApplicationState = globalApplicationState;
		this.authenticationController = authenticationController;
		this.cellFactory = cellFactory;
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, Callback widgetRefreshRequired, Long wikiVersionInView) {
		view.clear();
		// set up view based on descriptor parameters
		descriptor = widgetDescriptor;
		tableConfig = new APITableConfig(descriptor);

		// if the table is configured to only show if the user is logged in, and we are not logged in, then
		// just return.
		if (!authenticationController.isLoggedIn() && tableConfig.isShowOnlyIfLoggedIn())
			return;

		if (tableConfig.getUri() != null) {
			refreshData();
		} else {
			synAlert.showError(DisplayConstants.API_TABLE_MISSING_URI);
			view.showError(synAlert.asWidget());
		}
	}

	@Override
	public void pageBack() {
		tableConfig.setOffset(tableConfig.getOffset() - tableConfig.getPageSize());
		if (tableConfig.getOffset() < 0)
			tableConfig.setOffset(0);
		refreshData();
	}

	@Override
	public void pageForward() {
		tableConfig.setOffset(tableConfig.getOffset() + tableConfig.getPageSize());
		if (tableConfig.getOffset() > total)
			tableConfig.setOffset(total - tableConfig.getPageSize());
		refreshData();
	}

	@Override
	public void refreshData() {
		view.clear();
		String fullUri = tableConfig.getUri();

		if (tableConfig.isPaging()) {
			// SWC-1133: only modify the URI with Ordered By if we are paging (otherwise, JQuery tablesorter
			// will handle sorting)
			fullUri = getOrderedByURI(fullUri, tableConfig);
			fullUri = getPagedURI(fullUri);
		}

		if (authenticationController.isLoggedIn()) {
			String userId = authenticationController.getCurrentUserPrincipalId();
			fullUri = fullUri.replace(CURRENT_USER_SQL_VARIABLE, userId).replace(ENCODED_CURRENT_USER_SQL_VARIABLE, userId);
		}
		if (isSubmissionQueryService(fullUri) && tableConfig.getColumnConfigs() != null && tableConfig.getColumnConfigs().size() > 0) {
			// look for '*' in query. if found, replace with columns defined in the table config
			if (fullUri.contains("*")) {
				if (isValidColumnNames(tableConfig.getColumnConfigs())) {
					fullUri = fullUri.replace("*", getSelectColumns(tableConfig.getColumnConfigs()));
				}
			}
		}

		jsClient.getJSON(fullUri, new AsyncCallback<JSONObjectAdapter>() {
			@Override
			public void onSuccess(JSONObjectAdapter adapter) {
				try {
					if (adapter.has("totalNumberOfResults")) {
						total = adapter.getInt("totalNumberOfResults");
					}

					List<Row> rows = new ArrayList<>();
					List<String> columnNames = new ArrayList<>();

					if (tableConfig.isQueryTableResults()) {
						// initialize
						QueryTableResults results = new QueryTableResults();
						results.initializeFromJSONObject(adapter);
						columnNames = results.getHeaders();
						rowCount = results.getRows().size();
						rows = results.getRows();
					} else if (adapter.has(tableConfig.getJsonResultsArrayKeyName())) {
						JSONArrayAdapter resultsList = adapter.getJSONArray(tableConfig.getJsonResultsArrayKeyName());
						rowCount = resultsList.length();
						if (rowCount > 0) {
							JSONObjectAdapter firstItem = resultsList.getJSONObject(0);
							for (Iterator it = firstItem.keys(); it.hasNext();) {
								columnNames.add((String) it.next());
							}

							for (int i = 0; i < resultsList.length(); i++) {
								JSONObjectAdapter row = resultsList.getJSONObject(i);
								List<String> rowValues = new ArrayList<String>();
								for (String key : columnNames) {
									rowValues.add(getColumnValue(row, key));
								}
								Row r = new Row();
								r.setValues(rowValues);
								rows.add(r);
							}
						}
					}

					if (rowCount == 0 && tableConfig.getColumnConfigs().isEmpty()) {
						// no results, and no column configs (so we don't know the structure of the table). show nothing
						return;
					}

					// if node query, remove the object type from the column names (ie remove "project." from
					// "project.id")
					if (isNodeQueryService(tableConfig.getUri())) {
						columnNames = fixColumnNames(columnNames);
					}

					// if column configs were not passed in, then use default
					if (tableConfig.getColumnConfigs() == null || tableConfig.getColumnConfigs().size() == 0) {
						tableConfig.setColumnConfigs(getDefaultColumnConfigs(columnNames, tableConfig));
					}

					List<Integer> columnIndices = new ArrayList<>();
					List<ApiTableColumnType> columnTypes = new ArrayList<>();
					for (APITableColumnConfig config : tableConfig.getColumnConfigs()) {
						String rendererName = config.getRendererFriendlyName();
						columnTypes.add(getColumnType(rendererName));
						String inputColumnName = config.getInputColumnNames().iterator().next();
						columnIndices.add(columnNames.indexOf(inputColumnName));
					}
					view.clear();
					view.setColumnHeaders(tableConfig.getColumnConfigs());

					for (Row row : rows) {
						List<IsWidget> columnWidgets = new ArrayList<>();
						List<String> columnValues = row.getValues();
						for (int i = 0; i < columnIndices.size(); i++) {
							int index = columnIndices.get(i);
							String colValue = index > -1 ? columnValues.get(index) : "";
							columnWidgets.add(getNewCell(columnTypes.get(i), colValue));
						}
						view.addRow(columnWidgets);
					}

					if (tableConfig.isPaging() && total > tableConfig.getPageSize()) {
						int start = tableConfig.getOffset() + 1;
						int end = start + rowCount - 1;
						view.configurePager(start, end, total);
					} else {
						view.initializeTableSorter();
					}
				} catch (Exception e1) {
					onFailure(e1);
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				if (caught instanceof TableUnavilableException)
					view.showTableUnavailable();
				else {
					synAlert.handleException(caught);
					view.showError(synAlert.asWidget());
				}
			}
		});
	}

	public IsWidget getNewCell(ApiTableColumnType type, String value) {
		if (type.getSynapseTableColumnType() != null) {
			Cell cell = cellFactory.createRenderer(type.getSynapseTableColumnType());
			cell.setValue(value);
			return cell;
		} else if (ApiTableColumnType.CANCEL_CONTROL.equals(type)) {
			CancelControlWidget cancelRequestWidget = ginInjector.getCancelControlWidget();
			cancelRequestWidget.configure(value);
			return cancelRequestWidget;
		} else if (ApiTableColumnType.MARKDOWN_LINK.equals(type)) {
			MarkdownWidget mdWidget = ginInjector.getMarkdownWidget();
			mdWidget.configure(value);
			return mdWidget;
		} else {
			DivView div = ginInjector.getDiv();
			div.setText(value);
			return div;
		}
	}

	public boolean isValidColumnNames(List<APITableColumnConfig> configs) {
		for (APITableColumnConfig config : configs) {
			for (String columnName : config.getInputColumnNames()) {
				if (WORD_PATTERN.exec(columnName) == null) {
					return false;
				}
			}
		}
		return true;
	}

	public String getSelectColumns(List<APITableColumnConfig> configs) {
		StringBuilder columnNames = new StringBuilder();
		for (Iterator<APITableColumnConfig> iterator = configs.iterator(); iterator.hasNext();) {
			APITableColumnConfig config = iterator.next();
			String inputColumnName = config.getInputColumnNames().iterator().next();
			columnNames.append(inputColumnName);
			if (iterator.hasNext()) {
				columnNames.append(",");
			}
		}
		return columnNames.toString();
	}

	public static List<String> fixColumnNames(List<String> columnNames) {
		List<String> fixedColumnNames = null;
		if (columnNames != null) {
			fixedColumnNames = new ArrayList<>();
			for (String columnName : columnNames) {
				fixedColumnNames.add(removeFirstToken(columnName));
			}
		}
		return fixedColumnNames;
	}

	public static String removeFirstToken(String colName) {
		if (colName == null)
			return colName;
		int dotIndex = colName.indexOf('.');
		if (dotIndex > -1)
			return colName.substring(dotIndex + 1);
		else
			return colName;
	}

	public static String getColumnValue(JSONObjectAdapter row, String key) throws JSONObjectAdapterException {
		String value = "";
		if (row.has(key)) {
			try {
				Object objValue;
				// try to parse it as a String, then as a Long, then fall back to get object
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
				// try to get it as an array
				JSONArrayAdapter valueArray = row.getJSONArray(key);
				StringBuilder valueArraySB = new StringBuilder();
				for (int j = 0; j < valueArray.length(); j++) {
					Object objValue = valueArray.get(j);
					if (objValue != null)
						valueArraySB.append(objValue.toString() + ",");
				}
				if (valueArraySB.length() > 0) {
					valueArraySB.deleteCharAt(valueArraySB.length() - 1);
					value = valueArraySB.toString();
				}
			}
		}
		return value;
	}

	public String getPagedURI(String uri) {
		// special case for query service
		boolean isSubmissionQueryService = isSubmissionQueryService(uri);
		boolean isNodeQueryService = isNodeQueryService(uri);
		if (isSubmissionQueryService || isNodeQueryService) {
			// the node query service's first element is at index 1! (submission query service first element is
			// at index 0)
			Long firstIndex = isSubmissionQueryService ? ServiceConstants.DEFAULT_PAGINATION_OFFSET : 1;
			return uri + "+limit+" + tableConfig.getPageSize() + "+offset+" + (tableConfig.getOffset() + firstIndex);
		} else {
			String firstCharacter = uri.contains("?") ? "&" : "?";
			return uri + firstCharacter + "limit=" + tableConfig.getPageSize() + "&offset=" + tableConfig.getOffset();
		}
	}

	public String getOrderedByURI(String uri, APITableConfig tableConfig) {
		String newUri = uri;
		if (isQueryService(uri)) {
			// find the order by column
			COLUMN_SORT_TYPE sort = COLUMN_SORT_TYPE.NONE;
			String columnName = null;
			for (APITableColumnConfig columnConfig : tableConfig.getColumnConfigs()) {
				if (columnConfig.getSort() != null && COLUMN_SORT_TYPE.NONE != columnConfig.getSort()) {
					// found
					Set<String> inputColumnNames = columnConfig.getInputColumnNames();
					if (inputColumnNames.size() > 0) {
						// take one
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
			// if there is something to sort
			if (COLUMN_SORT_TYPE.NONE != sort) {
				newUri = newUri + "+order+by+%22" + columnName + "%22+" + sort.toString();
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
	 * The renderers are built directly from the table column configs. The view will tell us when column
	 * from this renderer was clicked by the user.
	 * 
	 * @param index
	 */
	@Override
	public void columnClicked(int index) {
		// usually handled by JQuery tablesorter plugin, but if this is a query service (evaluation or
		// regular query) then we should append to the uri an appropriate order by
		if (isQueryService(tableConfig.getUri())) {
			// reset offset
			tableConfig.setOffset(0);
			// set all column sort values
			APITableColumnConfig targetColumnConfig = tableConfig.getColumnConfigs().get(index);
			for (APITableColumnConfig config : tableConfig.getColumnConfigs()) {
				COLUMN_SORT_TYPE sort = COLUMN_SORT_TYPE.NONE;
				if (targetColumnConfig == config) {
					// flip to ASC if already DESC
					sort = COLUMN_SORT_TYPE.DESC == config.getSort() ? COLUMN_SORT_TYPE.ASC : COLUMN_SORT_TYPE.DESC;
				}
				config.setSort(sort);
			}
			// then refresh the data
			refreshData();
		}
	}

	private List<APITableColumnConfig> getDefaultColumnConfigs(List<String> columnNames, APITableConfig tableConfig) {
		List<APITableColumnConfig> defaultConfigs = new ArrayList<APITableColumnConfig>();
		// create a config for each column
		for (int i = 0; i < columnNames.size(); i++) {
			APITableColumnConfig newConfig = new APITableColumnConfig();
			String currentColumnName = columnNames.get(i);
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
	 * 
	 * @param columnName
	 * @return
	 */
	public String guessRendererFriendlyName(String columnName, APITableConfig tableConfig) {
		String defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE;
		if (columnName != null) {
			String lowerCaseColumnName = columnName.toLowerCase();
			if (userColumnNames.contains(lowerCaseColumnName)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID;
			} else if (dateColumnNames.contains(lowerCaseColumnName)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE;
			} else if (synapseIdColumnNames.contains(lowerCaseColumnName) || (isNodeQueryService(tableConfig.getUri()) && WebConstants.DEFAULT_COL_NAME_ID.equals(lowerCaseColumnName))) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID;
			} else if (lowerCaseColumnName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_CANCEL_CONTROL)) {
				defaultRendererName = WidgetConstants.API_TABLE_COLUMN_RENDERER_CANCEL_CONTROL;
			}
		}
		return defaultRendererName;
	}

	/**
	 * Resolve a friendly renderer name (the name used in the editor and markdown) to a column type.
	 * 
	 * @param friendlyName
	 * @return
	 */
	public static ApiTableColumnType getColumnType(String friendlyName) {
		ApiTableColumnType type = ApiTableColumnType.STRING;
		if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID))
			type = ApiTableColumnType.USERID;
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_DATE) || friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE))
			type = ApiTableColumnType.DATE;
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_LINK))
			type = ApiTableColumnType.MARKDOWN_LINK;
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID))
			type = ApiTableColumnType.ENTITYID;
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_ANNOTATIONS))
			type = ApiTableColumnType.LARGETEXT;
		else if (friendlyName.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_CANCEL_CONTROL))
			type = ApiTableColumnType.CANCEL_CONTROL;

		return type;
	}


	@SuppressWarnings("unchecked")
	public void clearState() {}

	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	// for testing only
	public void setTableConfig(APITableConfig tableConfig) {
		this.tableConfig = tableConfig;
	}
}
