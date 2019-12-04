package org.sagebionetworks.web.client.widget.entity.editor;

import java.util.List;
import java.util.Map;
import org.sagebionetworks.web.client.widget.WidgetEditorPresenter;
import org.sagebionetworks.web.client.widget.entity.dialog.DialogCallback;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class APITableConfigEditor implements WidgetEditorPresenter {

	private APITableConfigView view;
	private Map<String, String> descriptor;

	@Inject
	public APITableConfigEditor(APITableConfigView view) {
		this.view = view;
		view.initView();
	}

	@Override
	public void configure(WikiPageKey wikiKey, Map<String, String> widgetDescriptor, DialogCallback dialogCallback) {
		descriptor = widgetDescriptor;
		APITableConfig tableConfig = new APITableConfig(widgetDescriptor);
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
		// update widget descriptor from the view
		view.checkParams();
		descriptor.clear();
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, view.getApiUrl());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, view.isPaging().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_QUERY_TABLE_RESULTS, view.isQueryTableResults().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_SHOW_IF_LOGGED_IN, view.isShowIfLoggedInOnly().toString());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, view.getPageSize());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY, view.getJsonResultsKeyName());
		updateDescriptor(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE, view.getCssStyle());
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

	public static void updateDescriptorWithColumnConfigs(Map<String, String> descriptor, List<APITableColumnConfig> configs) {
		// clean up old column config definitions
		int index = 0;
		boolean foundConfig = descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + index);
		while (foundConfig) {
			descriptor.remove(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + index);
			index++;
			foundConfig = descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + index);
		}

		if (configs != null) {
			for (int i = 0; i < configs.size(); i++) {
				APITableColumnConfig config = configs.get(i);
				StringBuilder sb = new StringBuilder();
				sb.append(config.getRendererFriendlyName());
				sb.append(APITableConfig.FIELD_DELIMITER);
				String displayColumnName = "";
				if (config.getDisplayColumnName() != null)
					displayColumnName = config.getDisplayColumnName().trim();
				sb.append(WidgetEncodingUtil.encodeValue(displayColumnName));
				sb.append(APITableConfig.FIELD_DELIMITER);
				for (String columnName : config.getInputColumnNames()) {
					sb.append(columnName);
					sb.append(APITableConfig.COLUMN_NAMES_DELIMITER);
				}
				sb.append(APITableConfig.FIELD_DELIMITER);
				sb.append(config.getSort().toString());

				String columnConfigString = sb.toString();
				if (columnConfigString != null && columnConfigString.trim().length() > 0)
					descriptor.put(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + i, columnConfigString);
			}
		}
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
