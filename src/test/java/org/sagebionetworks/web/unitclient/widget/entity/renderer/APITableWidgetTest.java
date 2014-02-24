package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.sagebionetworks.repo.model.ObjectType;
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
import org.sagebionetworks.web.client.widget.entity.registration.WidgetConstants;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererSynapseID;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidget;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableWidgetView;
import org.sagebionetworks.web.shared.WikiPageKey;
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
		String expectedOffset = "1";
		widget.configure(testWikiKey, descriptor, null, null);
		String testServiceCall = ClientProperties.QUERY_SERVICE_PREFIX+"select+*+from+project";
		String pagedURI = widget.getPagedURI(testServiceCall);
		assertEquals(testServiceCall + "+limit+10+offset+"+expectedOffset, pagedURI.toLowerCase());
	}
	
	@Test
	public void testQueryServicePagingURISubmissionSearch() throws JSONObjectAdapterException {
		String expectedOffset = "0";
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
	
	
}
