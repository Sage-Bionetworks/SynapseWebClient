package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.shared.WidgetConstants;


/**
 * APITableConfig Package information relating to an api table (including service call path, column
 * configs). Is initialized from a widget descriptor map.
 */
public class APITableConfig {
	private String uri, jsonResultsArrayKeyName, cssStyleName;
	private boolean isPaging, isQueryTableResults, isShowOnlyIfLoggedIn;
	private int offset, pageSize;
	private List<APITableColumnConfig> columnConfigs;
	public final static String COLUMN_NAMES_DELIMITER = ";";
	public final static String FIELD_DELIMITER = ",";

	public APITableConfig(Map<String, String> descriptor) {
		uri = descriptor.get(WidgetConstants.API_TABLE_WIDGET_PATH_KEY);
		// always initialize column configs (could be an empty list)
		columnConfigs = parseTableColumnConfigs(descriptor);
		// SWC-3847: default paging to true (it's typically true for Leaderboards)
		isPaging = true;
		isQueryTableResults = false;
		isShowOnlyIfLoggedIn = false;
		jsonResultsArrayKeyName = "results";
		cssStyleName = "";

		if (uri != null) {
			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY)) {
				isPaging = Boolean.parseBoolean(descriptor.get(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY));
				if (isPaging) {
					// initialize the offset and pagesize
					offset = 0;
					if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY))
						pageSize = Integer.parseInt(descriptor.get(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY));
					else
						pageSize = 10;
				}
			}
			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_QUERY_TABLE_RESULTS)) {
				isQueryTableResults = Boolean.parseBoolean(descriptor.get(WidgetConstants.API_TABLE_WIDGET_QUERY_TABLE_RESULTS));
			}

			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_SHOW_IF_LOGGED_IN)) {
				isShowOnlyIfLoggedIn = Boolean.parseBoolean(descriptor.get(WidgetConstants.API_TABLE_WIDGET_SHOW_IF_LOGGED_IN));
			}

			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY)) {
				jsonResultsArrayKeyName = descriptor.get(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY);
			}

			if (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE)) {
				cssStyleName = descriptor.get(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE);
			}
		}
	}

	public static List<APITableColumnConfig> parseTableColumnConfigs(Map<String, String> descriptor) {
		List<APITableColumnConfig> columnConfigs = new ArrayList<APITableColumnConfig>();
		// reconstruct table column configs (if there are any)
		int i = 0;
		while (descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + i)) {
			String configString = descriptor.get(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + i);
			String[] parts = configString.split(FIELD_DELIMITER);
			if (parts.length < 3) {
				throw new IllegalArgumentException(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + i + ": Invalid configuration due to missing fields.");
			}
			try {
				APITableColumnConfig config = new APITableColumnConfig();
				config.setRendererFriendlyName(parts[0]);
				config.setDisplayColumnName(WidgetEncodingUtil.decodeValue(parts[1]));
				Set<String> inputColumnNames = new HashSet<String>();
				String[] inputColumns = parts[2].split(COLUMN_NAMES_DELIMITER);
				for (int j = 0; j < inputColumns.length; j++) {
					inputColumnNames.add(inputColumns[j]);
				}
				config.setInputColumnNames(inputColumnNames);
				if (parts.length > 3) {
					config.setSort(COLUMN_SORT_TYPE.valueOf(parts[3].toUpperCase()));
				} else
					config.setSort(COLUMN_SORT_TYPE.NONE);

				if (parts.length > 4) {
					// also has the number of decimal places that should be shown
					config.setDecimalPlaces(Integer.parseInt(parts[4]));
				}
				columnConfigs.add(config);
			} catch (Throwable t) {
				throw new RuntimeException(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + i + ":" + t.getMessage(), t);
			}
			i++;
		}
		return columnConfigs;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getJsonResultsArrayKeyName() {
		return jsonResultsArrayKeyName;
	}

	public void setJsonResultsArrayKeyName(String jsonResultsArrayKeyName) {
		this.jsonResultsArrayKeyName = jsonResultsArrayKeyName;
	}

	public String getCssStyleName() {
		return cssStyleName;
	}

	public void setCssStyleName(String cssStyleName) {
		this.cssStyleName = cssStyleName;
	}

	public boolean isPaging() {
		return isPaging;
	}

	public boolean isQueryTableResults() {
		return isQueryTableResults;
	}

	public void setQueryTableResults(boolean isQueryTableResults) {
		this.isQueryTableResults = isQueryTableResults;
	}

	public boolean isShowOnlyIfLoggedIn() {
		return isShowOnlyIfLoggedIn;
	}

	public void setShowOnlyIfLoggedIn(boolean isShowOnlyIfLoggedIn) {
		this.isShowOnlyIfLoggedIn = isShowOnlyIfLoggedIn;
	}


	public void setPaging(boolean isPaging) {
		this.isPaging = isPaging;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public List<APITableColumnConfig> getColumnConfigs() {
		return columnConfigs;
	}

	public void setColumnConfigs(List<APITableColumnConfig> columnConfigs) {
		this.columnConfigs = columnConfigs;
	}
}
