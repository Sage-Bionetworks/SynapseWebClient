package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.ServiceConstants;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONArrayAdapterImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.COLUMN_SORT_TYPE;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererSynapseID;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidgetView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.shared.exceptions.TableUnavilableException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableWidgetTest {
		
	private static final String TESTSERVICE_PATH = "/testservice";
	APITableWidget widget;
	APITableWidgetView mockView;
	SynapseClientAsync mockSynapseClient;
	JSONObjectAdapter mockJSONObjectAdapter;
	PortalGinInjector mockGinInjector;
	APITableColumnRendererSynapseID synapseIDColumnRenderer;
	APITableColumnRendererNone noneColumnRenderer;
	GlobalApplicationState mockGlobalApplicationState;
	AuthenticationController mockAuthenticationController;
	
	String testJSON = "{totalNumberOfResults=10,results={}}";
	Map<String, String> descriptor;
	JSONObjectAdapter testReturnJSONObject;
	WikiPageKey testWikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	String col1Name ="column 1";
	String col2Name ="column 2";
	public static final int COLUMN_ROW_COUNT = 10;
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockView = mock(APITableWidgetView.class);
		mockSynapseClient = mock(SynapseClientAsync.class);
		mockJSONObjectAdapter = mock(JSONObjectAdapter.class);
		mockGinInjector = mock(PortalGinInjector.class);
		mockGlobalApplicationState = mock(GlobalApplicationState.class);
		mockAuthenticationController = mock(AuthenticationController.class);
		noneColumnRenderer = new APITableColumnRendererNone();
		synapseIDColumnRenderer = new APITableColumnRendererSynapseID();
		
		testReturnJSONObject = new JSONObjectAdapterImpl();
		testReturnJSONObject.put("totalNumberOfResults", 100);
		//and create some results
		JSONObjectAdapter result1 = testReturnJSONObject.createNew();
		fillInResult(result1, new String[]{"field1", "field2"}, new String[]{"result1 value 1", "result1 value 2"});
		JSONObjectAdapter result2 = testReturnJSONObject.createNew();
		fillInResult(result2, new String[]{"field1", "field2"}, new String[]{"result2 value 1", "result2 value 2"});
		JSONArrayAdapter results = new JSONArrayAdapterImpl();
		results.put(0, result2);
		results.put(0, result1);
		testReturnJSONObject.put("results", results);
		when(mockJSONObjectAdapter.createNew(anyString())).thenReturn(testReturnJSONObject);
		
		when(mockGinInjector.getAPITableColumnRendererNone()).thenReturn(noneColumnRenderer);
		when(mockGinInjector.getAPITableColumnRendererSynapseID()).thenReturn(synapseIDColumnRenderer);
		
		AsyncMockStubber.callSuccessWith(testJSON).when(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		widget = new APITableWidget(mockView, mockSynapseClient, mockJSONObjectAdapter, mockGinInjector, mockGlobalApplicationState, mockAuthenticationController);
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, TESTSERVICE_PATH);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "true");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, "10");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY, "true");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY, "Row Number");
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
		verify(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).clear();
		verify(mockView).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
		verify(mockView).configurePager(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void testEmptyColumnSpecification() throws JSONObjectAdapterException {
		//remove everything but the uri
		
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY);
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY);
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY);
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY);
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY);
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE);
		
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockView).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
	}
	
	@Test
	public void testRendererFailure() {
		//even if the column renderer fails to initialize, everything should still work
		APITableColumnRendererSynapseID failColumnRenderer = new APITableColumnRendererSynapseID(){
			@Override
			public void init(Map<String, List<String>> columnData,
					APITableColumnConfig config,
					AsyncCallback<APITableInitializedColumnRenderer> callback) {
				callback.onFailure(new Exception("Load failure"));
			}
		};
		//return our renderer that always fails to initialize when asked for the Synapse ID column renderer
		when(mockGinInjector.getAPITableColumnRendererSynapseID()).thenReturn(failColumnRenderer);
		widget.configure(testWikiKey, descriptor, null, null);
		
		verify(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
		verify(mockView).configurePager(anyInt(), anyInt(), anyInt());
	}
	
	//test removing uri causes error to be shown
	@Test
	public void testMissingServiceURI() throws JSONObjectAdapterException {
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_PATH_KEY);
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockView).showError(anyString());
	}
	
	//test uri call failure causes view to render error
	@Test
	public void testServiceCallFailure() throws JSONObjectAdapterException {
		String errorMessage = "service response error message";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockView).showError(eq(errorMessage));
	}
	
	@Test
	public void testTableUnavailableFailure() throws JSONObjectAdapterException {
		AsyncMockStubber.callFailureWith(new TableUnavilableException()).when(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockView).showTableUnavailable();
	}

	
	@Test
	public void testNoPaging() throws JSONObjectAdapterException {
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "false");
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockView).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
		verify(mockView, Mockito.times(0)).configurePager(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void testPagerNotNecessary() throws JSONObjectAdapterException {
		testReturnJSONObject.put("totalNumberOfResults", 2);
		widget.configure(testWikiKey, descriptor, null, null);
		verify(mockView).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
		verify(mockView, Mockito.times(0)).configurePager(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void testPagingURI() throws JSONObjectAdapterException {
		widget.configure(testWikiKey, descriptor, null, null);
		String pagedURI = widget.getPagedURI(TESTSERVICE_PATH);
		assertEquals(TESTSERVICE_PATH + "?limit=10&offset=0", pagedURI.toLowerCase());
	}
	
	@Test
	public void testQueryServicePagingURINodeSearch() throws JSONObjectAdapterException {
		String expectedOffset = ServiceConstants.DEFAULT_PAGINATION_OFFSET_PARAM_NO_OFFSET_EQUALS_ONE;
		widget.configure(testWikiKey, descriptor, null, null);
		String testServiceCall = ClientProperties.QUERY_SERVICE_PREFIX+"select+*+from+project";
		String pagedURI = widget.getPagedURI(testServiceCall);
		assertEquals(testServiceCall + "+limit+10+offset+"+expectedOffset, pagedURI.toLowerCase());
	}
	
	@Test
	public void testQueryServicePagingURISubmissionSearch() throws JSONObjectAdapterException {
		String expectedOffset = ServiceConstants.DEFAULT_PAGINATION_OFFSET_PARAM_NEW;
		widget.configure(testWikiKey, descriptor, null, null);
		String testServiceCall = ClientProperties.EVALUATION_QUERY_SERVICE_PREFIX+"select+*+from+evaluation_1234";
		String pagedURI = widget.getPagedURI(testServiceCall);
		assertEquals(testServiceCall + "+limit+10+offset+"+expectedOffset, pagedURI.toLowerCase());
	}

	
	@Test
	public void testCurrentUserVariable() throws JSONObjectAdapterException {
		String testServiceCall = ClientProperties.QUERY_SERVICE_PREFIX+"select+*+from+project+where+userId==" + APITableWidget.CURRENT_USER_SQL_VARIABLE;
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, testServiceCall);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "false");
		when(mockAuthenticationController.isLoggedIn()).thenReturn(true);
		String testUserId = "12345test";
		when(mockAuthenticationController.getCurrentUserPrincipalId()).thenReturn(testUserId);
		
		widget.configure(testWikiKey, descriptor, null, null);
		
		ArgumentCaptor<String> arg = ArgumentCaptor.forClass(String.class);
		verify(mockSynapseClient).getJSONEntity(arg.capture(), any(AsyncCallback.class));
		
		assertTrue(arg.getValue().endsWith(testUserId));
	}
	
	@Test
	public void testLoggedInOnly() throws JSONObjectAdapterException {
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_SHOW_IF_LOGGED_IN, "true");
		when(mockAuthenticationController.isLoggedIn()).thenReturn(false);
		widget.configure(testWikiKey, descriptor, null, null);
		
		verify(mockView).clear();
		verify(mockView, times(0)).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
	}
	
	private Set<String> getTestColumnNameSet() {
		Set<String> testSet = new HashSet<String>();
		testSet.add(col1Name);
		testSet.add(col2Name);
		return testSet;
	}
	private List<String> getTestColumnValues(String columnName) {
		List<String> testColumnValues = new ArrayList<String>();
		for (int i = 0; i < COLUMN_ROW_COUNT; i++) {
			testColumnValues.add(columnName + " data item " + i);
		}
		return testColumnValues;
	}
	
	private Map<String, List<String>> getTestColumnData(List<String> columnNames) {
		Map<String, List<String>> colData = new HashMap<String, List<String>>();
		for (String colName : columnNames) {
			colData.put(colName, getTestColumnValues(colName));
		}
		return colData;
	}

	
	@Test
	public void testCreateColumnDataMap() throws JSONObjectAdapterException {
		Set<String> testSet = getTestColumnNameSet();
		Map<String, List<String>> dataMap = widget.createColumnDataMap(testSet.iterator());
		assertEquals(2, dataMap.keySet().size());
		assertEquals(new ArrayList<String>(), dataMap.get(col1Name));
	}
	
	@Test
	public void testCreateColumnDataMapEmptyOrNull() throws JSONObjectAdapterException {
		Map emptyMap = new HashMap<String, String>();
		Set<String> emptyTestSet = new HashSet<String>();
		Map<String, List<String>> dataMap = widget.createColumnDataMap(emptyTestSet.iterator());
		assertEquals(emptyMap, dataMap);
		dataMap = widget.createColumnDataMap(null);
		assertEquals(emptyMap, dataMap);
	}
	
	
	@Test
	public void testCreateRenderersNull() throws JSONObjectAdapterException {
		String[] columnNames = widget.getColumnNamesArray(getTestColumnNameSet());
		APITableConfig newConfig = new APITableConfig(descriptor);
		newConfig.setColumnConfigs(null);
		widget.createRenderers(columnNames, newConfig, mockGinInjector);
		//should have tried to create two default renderers (NONE)
		verify(mockGinInjector, times(2)).getAPITableColumnRendererNone();
	}

	@Test
	public void testCreateRenderersEmpty() throws JSONObjectAdapterException {
		String[] columnNames = widget.getColumnNamesArray(getTestColumnNameSet());
		APITableConfig newConfig = new APITableConfig(descriptor);
		newConfig.setColumnConfigs(new ArrayList());
		widget.createRenderers(columnNames, newConfig, mockGinInjector);
		//should have tried to create two default renderers (NONE)
		verify(mockGinInjector, times(2)).getAPITableColumnRendererNone();
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
	public void testCreateRenderer() throws JSONObjectAdapterException {
		String[] columnNames = new String[]{col1Name, col2Name};
		
		widget.createRenderers(columnNames, getTableConfig(), mockGinInjector);
		//should have tried to create a user id renderer (based on the table configuration)
		verify(mockGinInjector).getAPITableColumnRendererUserId();
		verify(mockGinInjector).getAPITableColumnRendererSynapseID();
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
		//and set a sort column
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
		widget.columnConfigClicked(sortColumnConfigs.get(0));
		inputUri = widget.getOrderedByURI(inputUri, tableConfig).toLowerCase();
		assertTrue(inputUri.contains("order+by+"));
		assertTrue(inputUri.contains("desc"));
		sortColumnConfigs.get(1).setSort(COLUMN_SORT_TYPE.DESC);
		widget.columnConfigClicked(sortColumnConfigs.get(1));
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
		//case should not matter
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_USER_ID.toUpperCase(), tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_USER_ID.toLowerCase(), tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_MODIFIED_BY_PRINCIPAL_ID, tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_CREATED_ON, tableConfig));
		
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_ENTITY_ID, tableConfig));
		assertEquals(WidgetConstants.API_TABLE_COLUMN_RENDERER_SYNAPSE_ID, widget.guessRendererFriendlyName(WebConstants.DEFAULT_COL_NAME_PARENT_ID, tableConfig));
		
		//next, check "id".  If node query service, assume it's a synapse id.  Otherwise, do not render in a special way.
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
		Map<String, List<String>> columnData = getTestColumnData(colNames);
		
		APITableWidget.fixColumnNames(columnData);
		
		//no longer contains project.id, but does contain id
		assertFalse(columnData.containsKey(column1));
		assertTrue(columnData.containsKey("id"));
		//still contains name
		assertTrue(columnData.containsKey(column2));
	}
	
	@Test
	public void testGetColumnValues() {
		String column1 = "id";  
		List<String> colNames = new ArrayList<String>();
		colNames.add(column1);
		Map<String, List<String>> columnData = getTestColumnData(colNames);
		
		assertNotNull(APITableWidget.getColumnValues(column1, columnData));
		//previous table column definitions will be looking for the type. This should also work
		assertNotNull(APITableWidget.getColumnValues("project."+column1, columnData));
		//absent column should not be null, and should have the item count 
		List<String> absentColumn = APITableWidget.getColumnValues("absent", columnData);
		assertNotNull(absentColumn);
		assertEquals(COLUMN_ROW_COUNT, absentColumn.size());
		assertNull(absentColumn.get(0));
	}
}
