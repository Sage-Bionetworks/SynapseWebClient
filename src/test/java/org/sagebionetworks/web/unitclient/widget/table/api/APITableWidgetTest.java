package org.sagebionetworks.web.unitclient.widget.table.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.repo.model.table.ColumnType;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONArrayAdapterImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.widget.entity.MarkdownWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.renderer.CancelControlWidget;
import org.sagebionetworks.web.client.widget.table.api.APITableWidget;
import org.sagebionetworks.web.client.widget.table.api.APITableWidgetView;
import org.sagebionetworks.web.client.widget.table.api.ApiTableColumnType;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.Cell;
import org.sagebionetworks.web.client.widget.table.v2.results.cell.CellFactory;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.TableUnavilableException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

@RunWith(MockitoJUnitRunner.class)
public class APITableWidgetTest {
	public static final String COL_1_RESULT_VALUE_1 = "result1 value 1";

	private static final String TESTSERVICE_PATH = "/testservice";

	APITableWidget widget;

	@Mock
	APITableWidgetView mockView;
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	PortalGinInjector mockGinInjector;
	@Mock
	GlobalApplicationState mockGlobalApplicationState;
	@Mock
	AuthenticationController mockAuthenticationController;
	@Mock
	CancelControlWidget mockCancelControlWidget;
	@Mock
	MarkdownWidget mockMarkdownWidget;

	Map<String, String> descriptor;
	JSONObjectAdapter testReturnJSONObject;
	WikiPageKey testWikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	String col1Name = "column_1";
	String col2Name = "column_2";
	public static final int COLUMN_ROW_COUNT = 10;
	@Mock
	SynapseAlert mockSynAlert;
	@Mock
	CellFactory mockCellFactory;
	@Captor
	ArgumentCaptor<String> stringCaptor;
	@Mock
	Cell mockCell;
	

	@Before
	public void setup() throws JSONObjectAdapterException {
		when(mockCellFactory.createRenderer(any(ColumnType.class))).thenReturn(mockCell);
		when(mockGinInjector.getCancelControlWidget()).thenReturn(mockCancelControlWidget);
		when(mockGinInjector.getMarkdownWidget()).thenReturn(mockMarkdownWidget);
		testReturnJSONObject = new JSONObjectAdapterImpl();
		testReturnJSONObject.put("totalNumberOfResults", 100);
		// and create some results
		JSONObjectAdapter result1 = testReturnJSONObject.createNew();
		fillInResult(result1, new String[] {col1Name, col2Name}, new String[] {COL_1_RESULT_VALUE_1, "result1 value 2"});
		JSONObjectAdapter result2 = testReturnJSONObject.createNew();
		fillInResult(result2, new String[] {col1Name, col2Name}, new String[] {"result2 value 1", "result2 value 2"});
		JSONArrayAdapter results = new JSONArrayAdapterImpl();
		results.put(0, result2);
		results.put(0, result1);
		testReturnJSONObject.put("results", results);

		AsyncMockStubber.callSuccessWith(testReturnJSONObject).when(mockSynapseJavascriptClient).getJSON(anyString(), any(AsyncCallback.class));
		widget = new APITableWidget(mockView, mockSynapseJavascriptClient, mockGinInjector, mockGlobalApplicationState, mockAuthenticationController, mockSynAlert, mockCellFactory);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, TESTSERVICE_PATH);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "true");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, "10");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY, "results");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE, "myTableStyle");
	}

	private void fillInResult(JSONObjectAdapter result, String[] fieldNames, String[] fieldValues) throws JSONObjectAdapterException {
		for (int i = 0; i < fieldNames.length; i++) {
			result.put(fieldNames[i], fieldValues[i]);
		}
	}

	@Test
	public void testAsWidget() {
		widget.asWidget();
		verify(mockView).asWidget();
	}

	@Test
	public void testConfigure() {
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockSynapseJavascriptClient).getJSON(anyString(), any(AsyncCallback.class));
		verify(mockView, times(3)).clear();
		verify(mockView).setColumnHeaders(anyList());
		verify(mockView).addRow(anyList());
		verify(mockView).configurePager(anyInt(), anyInt(), anyInt());
	}

	@Test
	public void testConfigureWithTableConfigValidColumnName() {
		List<APITableColumnConfig> columnConfigs = new ArrayList<>();
		APITableColumnConfig columnConfig = new APITableColumnConfig();
		columnConfig.setInputColumnNames(Collections.singleton(col1Name));
		columnConfig.setSort(COLUMN_SORT_TYPE.NONE);
		columnConfigs.add(columnConfig);
		APITableConfigEditor.updateDescriptorWithColumnConfigs(descriptor, columnConfigs);
		String uri = ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX + "select+*+from+evaluation_1";
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, uri);

		widget.configure(testWikiKey, descriptor, null, null);

		// verify selects column name only
		verify(mockSynapseJavascriptClient).getJSON(stringCaptor.capture(), any(AsyncCallback.class));
		String actualUri = stringCaptor.getValue();
		assertTrue(actualUri.contains(col1Name));
		assertFalse(actualUri.contains("*"));
		verify(mockView).addRow(anyList());
		verify(mockCell).setValue(COL_1_RESULT_VALUE_1);
	}

	@Test
	public void testConfigureWithTableConfigInvalidColumnName() {
		List<APITableColumnConfig> columnConfigs = new ArrayList<>();
		APITableColumnConfig columnConfig = new APITableColumnConfig();
		columnConfig.setInputColumnNames(Collections.singleton("invalid-name"));
		columnConfig.setSort(COLUMN_SORT_TYPE.NONE);
		columnConfigs.add(columnConfig);
		APITableConfigEditor.updateDescriptorWithColumnConfigs(descriptor, columnConfigs);
		String uri = ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX + "select+*+from+evaluation_1";
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, uri);

		widget.configure(testWikiKey, descriptor, null, null);

		// verify selects *
		verify(mockSynapseJavascriptClient).getJSON(stringCaptor.capture(), any(AsyncCallback.class));
		String actualUri = stringCaptor.getValue();
		assertTrue(actualUri.contains(uri));
		verify(mockView).addRow(anyList());
		verify(mockCell).setValue("");
	}

	@Test
	public void testEmptyColumnSpecification() throws JSONObjectAdapterException {
		// remove everything but the uri

		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY);
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY);
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY);
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE);

		widget.configure(testWikiKey, descriptor, null, null);

		verify(mockView).setColumnHeaders(anyList());
		verify(mockView).addRow(anyList());
	}


	@Test
	public void testEmptyResultsEmptyColumnConfiguration() throws JSONObjectAdapterException {
		// SWC-4022: if no results, and no column configuration, then show nothing (unknown table
		// structure).
		testReturnJSONObject.put("results", new JSONArrayAdapterImpl());

		widget.configure(testWikiKey, descriptor, null, null);

		verify(mockView, never()).setColumnHeaders(anyList());
		verify(mockView, never()).addRow(anyList());
	}

	// test removing uri causes error to be shown
	@Test
	public void testMissingServiceURI() throws JSONObjectAdapterException {
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_PATH_KEY);
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockSynAlert).showError(DisplayConstants.API_TABLE_MISSING_URI);
		verify(mockView).showError(any(Widget.class));
	}

	// test uri call failure causes view to render error
	@Test
	public void testServiceCallFailure() throws JSONObjectAdapterException {
		String errorMessage = "service response error message";
		Exception ex = new Exception(errorMessage);
		AsyncMockStubber.callFailureWith(ex).when(mockSynapseJavascriptClient).getJSON(anyString(), any(AsyncCallback.class));
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockSynAlert).handleException(ex);
		verify(mockView).showError(any(Widget.class));
	}

	@Test
	public void testTableUnavailableFailure() throws JSONObjectAdapterException {
		AsyncMockStubber.callFailureWith(new TableUnavilableException()).when(mockSynapseJavascriptClient).getJSON(anyString(), any(AsyncCallback.class));
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockView).showTableUnavailable();
	}


	@Test
	public void testNoPaging() throws JSONObjectAdapterException {
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "false");

		widget.configure(testWikiKey, descriptor, null, null);

		verify(mockView).setColumnHeaders(anyList());
		verify(mockView).addRow(anyList());
		verify(mockView, never()).configurePager(anyInt(), anyInt(), anyInt());
	}

	@Test
	public void testPagerNotNecessary() throws JSONObjectAdapterException {
		testReturnJSONObject.put("totalNumberOfResults", 2);
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockView).setColumnHeaders(anyList());
		verify(mockView).addRow(anyList());
		verify(mockView, never()).configurePager(anyInt(), anyInt(), anyInt());
	}

	@Test
	public void testPagingURI() throws JSONObjectAdapterException {
		widget.configure(testWikiKey, descriptor, null, null);
		String pagedURI = widget.getPagedURI(TESTSERVICE_PATH);
		assertEquals(TESTSERVICE_PATH + "?limit=10&offset=0", pagedURI.toLowerCase());
	}

	@Test
	public void testQueryServicePagingURINodeSearch() throws JSONObjectAdapterException {
		String expectedOffset = "1";
		widget.configure(testWikiKey, descriptor, null, null);
		String testServiceCall = ClientProperties.QUERY_SERVICE_PREFIX + "select+*+from+project";
		String pagedURI = widget.getPagedURI(testServiceCall);
		assertEquals(testServiceCall + "+limit+10+offset+" + expectedOffset, pagedURI.toLowerCase());
	}

	@Test
	public void testQueryServicePagingURISubmissionSearch() throws JSONObjectAdapterException {
		String expectedOffset = ServiceConstants.DEFAULT_PAGINATION_OFFSET_PARAM;
		widget.configure(testWikiKey, descriptor, null, null);
		String testServiceCall = ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX + "select+*+from+evaluation_1234";
		String pagedURI = widget.getPagedURI(testServiceCall);
		assertEquals(testServiceCall + "+limit+10+offset+" + expectedOffset, pagedURI.toLowerCase());
	}


	@Test
	public void testCurrentUserVariable() throws JSONObjectAdapterException {
		String testServiceCall = ClientProperties.QUERY_SERVICE_PREFIX + "select+*+from+project+where+userId==" + APITableWidget.CURRENT_USER_SQL_VARIABLE;
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, testServiceCall);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "false");
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		String testUserId = "12345test";
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(testUserId);

		widget.configure(testWikiKey, descriptor, null, null);

		ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseJavascriptClient).getJSON(arg.capture(), any(AsyncCallback.class));

		assertTrue(arg.getValue().endsWith(testUserId));
	}

	@Test
	public void testCurrentUserVariableEncoded() throws JSONObjectAdapterException {
		String testServiceCall = ClientProperties.QUERY_SERVICE_PREFIX + "select+*+from+project+where+userId==" + APITableWidget.ENCODED_CURRENT_USER_SQL_VARIABLE;
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, testServiceCall);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "false");
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		String testUserId = "12345test";
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(testUserId);

		widget.configure(testWikiKey, descriptor, null, null);

		ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseJavascriptClient).getJSON(arg.capture(), any(AsyncCallback.class));

		assertTrue(arg.getValue().endsWith(testUserId));
	}

	@Test
	public void testLoggedInOnly() throws JSONObjectAdapterException {
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_SHOW_IF_LOGGED_IN, "true");
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.configure(testWikiKey, descriptor, null, null);

		verify(mockView).clear();
		verify(mockView, never()).setColumnHeaders(anyList());
		verify(mockView, never()).addRow(anyList());
	}

	private APITableConfig getTableConfig() {
		APITableConfig tableConfig = new APITableConfig(descriptor);
		List<APITableColumnConfig> configList = new ArrayList<APITableColumnConfig>();
		APITableColumnConfig columnConfig = new APITableColumnConfig();
		Set<String> inputColName = new HashSet<String>();
		inputColName.add(col1Name);
		columnConfig.setInputColumnNames(inputColName);
		columnConfig.setRendererFriendlyName(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID);
		configList.add(columnConfig);

		columnConfig = new APITableColumnConfig();
		inputColName = new HashSet<String>();
		inputColName.add(col2Name);
		columnConfig.setInputColumnNames(inputColName);
		columnConfig.setRendererFriendlyName(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID);
		configList.add(columnConfig);

		tableConfig.setColumnConfigs(configList);
		return tableConfig;
	}

	@Test
	public void testOrderByURINotQueryService() throws JSONObjectAdapterException {
		APITableConfig tableConfig = getTableConfig();
		String inputUri = "/evaluation";
		assertEquals(inputUri, widget.getOrderedByURI(inputUri, tableConfig));
		inputUri = "";
		assertEquals(inputUri, widget.getOrderedByURI(inputUri, tableConfig));
	}

	@Test
	public void testOrderByURIWithQueryService() throws JSONObjectAdapterException {
		APITableConfig tableConfig = getTableConfig();
		// and set a sort column
		APITableColumnConfig sortColumnConfig = tableConfig.getColumnConfigs().get(1);
		sortColumnConfig.setSort(COLUMN_SORT_TYPE.DESC);
		String inputUri = ClientProperties.QUERY_SERVICE_PREFIX + "select+*+from+project";
		String outputUri = widget.getOrderedByURI(inputUri, tableConfig).toLowerCase();
		assertTrue(outputUri.contains("order+by+"));
		assertTrue(outputUri.contains("desc"));

		inputUri = ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX + "select+*+from+evaluation_123";
		outputUri = widget.getOrderedByURI(inputUri, tableConfig).toLowerCase();
		assertTrue(outputUri.contains("order+by+"));
		assertTrue(outputUri.contains("desc"));

		sortColumnConfig.setSort(COLUMN_SORT_TYPE.NONE);
		outputUri = widget.getOrderedByURI(inputUri, tableConfig).toLowerCase();
		assertFalse(outputUri.contains("order+by+"));
		assertFalse(outputUri.contains("desc"));

		// also verify that the query case is not be altered (SWC-2569)
		inputUri = ClientProperties.QUERY_SERVICE_PREFIX + "select+*+from+folder+where+projectId='syn123'";
		outputUri = widget.getOrderedByURI(inputUri, tableConfig);
		assertTrue(outputUri.contains("projectId"));
	}

	@Test
	public void testRemoveFirstToken() {
		assertEquals("id", APITableWidget.removeFirstToken("project.id"));
		assertEquals("Annotation", APITableWidget.removeFirstToken("data.Annotation"));
		assertEquals("date", APITableWidget.removeFirstToken("date"));
		assertNull(APITableWidget.removeFirstToken(null));
	}

	@Test
	public void testColumnConfigClickedSorting() {
		String inputUri = ClientProperties.QUERY_SERVICE_PREFIX + "select+*+from+project";
		APITableConfig tableConfig = getTableConfig();
		tableConfig.setUri(inputUri);
		widget.setTableConfig(tableConfig);
		List<APITableColumnConfig> sortColumnConfigs = tableConfig.getColumnConfigs();
		sortColumnConfigs.get(0).setSort(COLUMN_SORT_TYPE.NONE);
		sortColumnConfigs.get(1).setSort(COLUMN_SORT_TYPE.DESC);
		widget.columnClicked(0);
		inputUri = widget.getOrderedByURI(inputUri, tableConfig).toLowerCase();
		assertTrue(inputUri.contains("order+by+"));
		assertTrue(inputUri.contains("desc"));
		sortColumnConfigs.get(1).setSort(COLUMN_SORT_TYPE.DESC);
		widget.columnClicked(1);
		inputUri = widget.getOrderedByURI(inputUri, tableConfig).toLowerCase();
		assertTrue(inputUri.contains("order+by+"));
		assertTrue(inputUri.contains("asc"));
		assertFalse(inputUri.contains("desc"));
	}

	@Test
	public void testGuessRendererFriendlyName() {
		APITableConfig tableConfig = getTableConfig();
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE, widget.guessRendererFriendlyName(null, tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE, widget.guessRendererFriendlyName("foo", tableConfig));
		// case should not matter
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_USER_ID.toUpperCase(), tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_USER_ID.toLowerCase(), tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_MODIFIED_BY_PRINCIPAL_ID, tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_CREATED_ON, tableConfig));

		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_ENTITY_ID, tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_PARENT_ID, tableConfig));

		// next, check "id". If node query service, assume it's a synapse id. Otherwise, do not render in a
		// special way.
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_ID, tableConfig));
		tableConfig.setUri(ClientProperties.QUERY_SERVICE_PREFIX + "select+*+from+project");
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_ID, tableConfig));
		tableConfig.setUri(ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX + "select+*+from+evaluation_123");
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_NONE, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_ID, tableConfig));
	}

	@Test
	public void testGetColumnValueKeyMissing() throws JSONObjectAdapterException {
		JSONObjectAdapter row = mock(JSONObjectAdapter.class);
		when(row.has(anyString())).thenReturn(false);
		assertEquals("", APITableWidget.getColumnValue(row, "key"));
	}

	@Test
	public void testGetColumnValueString() throws JSONObjectAdapterException {
		String testValue = "test";
		JSONObjectAdapter row = mock(JSONObjectAdapter.class);
		when(row.has(anyString())).thenReturn(true);
		when(row.getString(anyString())).thenReturn(testValue);
		assertEquals(testValue, APITableWidget.getColumnValue(row, "key"));
	}

	@Test
	public void testGetColumnValueLong() throws JSONObjectAdapterException {
		Long testValue = 10L;
		JSONObjectAdapter row = mock(JSONObjectAdapter.class);
		when(row.has(anyString())).thenReturn(true);
		when(row.getString(anyString())).thenThrow(new JSONObjectAdapterException("invalid string"));
		when(row.getLong(anyString())).thenReturn(testValue);
		assertEquals(testValue.toString(), APITableWidget.getColumnValue(row, "key"));
	}

	@Test
	public void testGetColumnValueDouble() throws JSONObjectAdapterException {
		Double testValue = 10.5;
		JSONObjectAdapter row = mock(JSONObjectAdapter.class);
		when(row.has(anyString())).thenReturn(true);
		when(row.getString(anyString())).thenThrow(new JSONObjectAdapterException("invalid string"));
		when(row.getLong(anyString())).thenThrow(new JSONObjectAdapterException("invalid long"));
		when(row.get(anyString())).thenReturn(testValue);
		assertEquals(testValue.toString(), APITableWidget.getColumnValue(row, "key"));
	}

	@Test
	public void testGetColumnValueArray() throws JSONObjectAdapterException {
		String val1 = "a";
		String val2 = "b";
		JSONArrayAdapter valueArray = mock(JSONArrayAdapter.class);
		when(valueArray.length()).thenReturn(2);
		when(valueArray.get(0)).thenReturn(val1);
		when(valueArray.get(1)).thenReturn(val2);

		JSONObjectAdapter row = mock(JSONObjectAdapter.class);
		when(row.has(anyString())).thenReturn(true);
		when(row.getString(anyString())).thenThrow(new JSONObjectAdapterException("invalid string"));
		when(row.getLong(anyString())).thenThrow(new JSONObjectAdapterException("invalid long"));
		when(row.get(anyString())).thenThrow(new JSONObjectAdapterException("invalid json object"));
		when(row.getJSONArray(anyString())).thenReturn(valueArray);

		assertEquals("a,b", APITableWidget.getColumnValue(row, "key"));
	}

	@Test
	public void testFixColumnNames() {
		String column1 = "project.id";
		String column2 = "name";
		List<String> colNames = new ArrayList<String>();
		colNames.add(column1);
		colNames.add(column2);

		List<String> fixedColumnNames = APITableWidget.fixColumnNames(colNames);

		// no longer contains project.id, but does contain id
		assertFalse(fixedColumnNames.contains(column1));
		assertTrue(fixedColumnNames.contains("id"));
		// still contains name
		assertTrue(fixedColumnNames.contains(column2));
	}

	@Test
	public void testGetSelectColumns() {
		String selectColumns = widget.getSelectColumns(getTableConfig().getColumnConfigs());
		assertEquals(col1Name + "," + col2Name, selectColumns);
	}

	@Test
	public void testGetColumnTypeFromRendererName() {
		// get the ApiTableColumnType based on the friendly name
		assertEquals(ApiTableColumnType.USERID, APITableWidget.getColumnType(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID));
		assertEquals(ApiTableColumnType.DATE, APITableWidget.getColumnType(WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE));
		assertEquals(ApiTableColumnType.MARKDOWN_LINK, APITableWidget.getColumnType(WidgetConstants.API_TABLE_COLUMN_RENDERER_LINK));
		assertEquals(ApiTableColumnType.ENTITYID, APITableWidget.getColumnType(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID));
		assertEquals(ApiTableColumnType.LARGETEXT, APITableWidget.getColumnType(WidgetConstants.API_TABLE_COLUMN_RENDERER_ANNOTATIONS));
		assertEquals(ApiTableColumnType.CANCEL_CONTROL, APITableWidget.getColumnType(WidgetConstants.API_TABLE_COLUMN_RENDERER_CANCEL_CONTROL));
	}

	@Test
	public void testGetNewCellCellRenderer() {
		// get new cell based on different api table column types
		String value = "1";
		IsWidget w = widget.getNewCell(ApiTableColumnType.STRING, value);
		assertEquals(w, mockCell);
		verify(mockCell).setValue(value);

		value = "2";
		IsWidget cancelControlCell = widget.getNewCell(ApiTableColumnType.CANCEL_CONTROL, value);
		assertEquals(cancelControlCell, mockCancelControlWidget);
		verify(mockCancelControlWidget).configure(value);

		value = "3";
		IsWidget markdownLinkCell = widget.getNewCell(ApiTableColumnType.MARKDOWN_LINK, value);
		assertEquals(markdownLinkCell, mockMarkdownWidget);
		verify(mockMarkdownWidget).configure(value);
	}

}
