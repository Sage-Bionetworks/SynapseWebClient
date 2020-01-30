package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_WIDGET_CSS_STYLE;
import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_WIDGET_PAGING_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_WIDGET_PATH_KEY;
import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.registration.WidgetEncodingUtil;
import org.sagebionetworks.web.shared.WidgetConstants;

public class APITableConfigTest {

	Map<String, String> descriptor;
	String path = "/evaluation";
	String isPaging = Boolean.TRUE.toString();
	String pageSize = new Integer(42).toString();
	String jsonResultsArrayKeyName = "myResultsField";
	String cssStyleName = "myCssStyle";

	@Before
	public void before() {
		// (without column configs)
		descriptor = new HashMap<String, String>();

		descriptor.put(API_TABLE_WIDGET_PATH_KEY, path);
		descriptor.put(API_TABLE_WIDGET_PAGING_KEY, isPaging);
		descriptor.put(API_TABLE_WIDGET_PAGESIZE_KEY, pageSize);
		descriptor.put(API_TABLE_WIDGET_RESULTS_KEY, jsonResultsArrayKeyName);
		descriptor.put(API_TABLE_WIDGET_CSS_STYLE, cssStyleName);
	}

	@Test
	public void testBasicParse() {
		APITableConfig tableconfig = new APITableConfig(descriptor);
		assertEquals(path, tableconfig.getUri());
		assertEquals(Boolean.parseBoolean(isPaging), tableconfig.isPaging());

		assertEquals(Integer.parseInt(pageSize), tableconfig.getPageSize());
		assertEquals(jsonResultsArrayKeyName, tableconfig.getJsonResultsArrayKeyName());
		assertEquals(cssStyleName, tableconfig.getCssStyleName());
		assertTrue(tableconfig.getColumnConfigs().size() == 0);
	}

	@Test
	public void testTableColumnConfigsHappyCase() {
		StringBuilder sb = new StringBuilder();
		// column 1 renderer
		sb.append(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE);
		sb.append(APITableConfig.FIELD_DELIMITER);
		String displayColumnName = "Column Name with " + APITableConfig.FIELD_DELIMITER + " delimiters" + APITableConfig.COLUMN_NAMES_DELIMITER;
		sb.append(WidgetEncodingUtil.encodeValue(displayColumnName));
		sb.append(APITableConfig.FIELD_DELIMITER);
		String inputColumn1 = "column1Name";
		String inputColumn2 = "column2Name";
		sb.append(inputColumn1);
		sb.append(APITableConfig.COLUMN_NAMES_DELIMITER);
		sb.append(inputColumn2);
		sb.append(APITableConfig.COLUMN_NAMES_DELIMITER);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 0, sb.toString());

		// column 2 renderer
		sb = new StringBuilder();
		sb.append(WidgetConstants.API_TABLE_COLUMN_RENDERER_DATE);
		sb.append(APITableConfig.FIELD_DELIMITER);
		String secondDisplayColumnName = "Second Column";
		sb.append(WidgetEncodingUtil.encodeValue(secondDisplayColumnName));
		sb.append(APITableConfig.FIELD_DELIMITER);
		String inputColumn3 = "column3Name";
		String inputColumn4 = "column4Name";
		sb.append(inputColumn3);
		sb.append(APITableConfig.COLUMN_NAMES_DELIMITER);
		sb.append(inputColumn4);
		sb.append(APITableConfig.COLUMN_NAMES_DELIMITER);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 1, sb.toString());

		List<APITableColumnConfig> configs = APITableConfig.parseTableColumnConfigs(descriptor);

		assertTrue(configs.size() == 2);
		APITableColumnConfig firstColumn = configs.get(0);
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE, firstColumn.getRendererFriendlyName());
		assertEquals(displayColumnName, firstColumn.getDisplayColumnName());
		assertTrue(firstColumn.getInputColumnNames().contains(inputColumn1));
		assertTrue(firstColumn.getInputColumnNames().contains(inputColumn2));

		APITableColumnConfig secondColumn = configs.get(1);
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_DATE, secondColumn.getRendererFriendlyName());
		assertEquals(secondDisplayColumnName, secondColumn.getDisplayColumnName());
		assertTrue(secondColumn.getInputColumnNames().contains(inputColumn3));
		assertTrue(secondColumn.getInputColumnNames().contains(inputColumn4));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTableColumnConfigsBadPartCount() {
		StringBuilder sb = new StringBuilder();
		// column 1 renderer
		sb.append(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE);
		sb.append(APITableConfig.FIELD_DELIMITER);
		String displayColumnName = "Column Name with " + APITableConfig.FIELD_DELIMITER + " delimiters" + APITableConfig.COLUMN_NAMES_DELIMITER;
		sb.append(WidgetEncodingUtil.encodeValue(displayColumnName));
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 0, sb.toString());

		try {
			APITableConfig.parseTableColumnConfigs(descriptor);
		} catch (IllegalArgumentException e) {
			// somewhere in the message it should point to the offending column config string
			assertTrue(e.getMessage().indexOf(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 0) > -1);
			throw e;
		}
	}

	@Test
	public void testUpdateDescriptorWithColumnConfigsCleanup() {
		String oldDefinition = "outofdate";
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 0, oldDefinition);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 1, oldDefinition);

		List<APITableColumnConfig> configs = new ArrayList<APITableColumnConfig>();
		APITableColumnConfig newColumnDefinition = new APITableColumnConfig();
		newColumnDefinition.setInputColumnNames(new HashSet<String>());
		newColumnDefinition.setSort(COLUMN_SORT_TYPE.NONE);
		configs.add(newColumnDefinition);
		APITableConfigEditor.updateDescriptorWithColumnConfigs(descriptor, configs);

		// first definition updated
		assertTrue(descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 0));
		assertNotSame(oldDefinition, descriptor.get(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 0));
		// second definition removed
		assertFalse(descriptor.containsKey(WidgetConstants.API_TABLE_WIDGET_COLUMN_CONFIG_PREFIX + 1));
	}
}
