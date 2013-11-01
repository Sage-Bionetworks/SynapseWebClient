package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;
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
		widget.configure(testWikiKey, descriptor, null);
		verify(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
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
		
		widget.configure(testWikiKey, descriptor, null);
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
		widget.configure(testWikiKey, descriptor, null);
		
		verify(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		verify(mockView).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
		verify(mockView).configurePager(anyInt(), anyInt(), anyInt());
	}
	
	//test removing uri causes error to be shown
	@Test
	public void testMissingServiceURI() throws JSONObjectAdapterException {
		descriptor.remove(WidgetConstants.API_TABLE_WIDGET_PATH_KEY);
		widget.configure(testWikiKey, descriptor, null);
		verify(mockView).showError(anyString());
	}
	
	//test uri call failure causes view to render error
	@Test
	public void testServiceCallFailure() throws JSONObjectAdapterException {
		String errorMessage = "service response error message";
		AsyncMockStubber.callFailureWith(new Exception(errorMessage)).when(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		widget.configure(testWikiKey, descriptor, null);
		verify(mockView).showError(eq(errorMessage));
	}
	
	@Test
	public void testNoPaging() throws JSONObjectAdapterException {
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "false");
		widget.configure(testWikiKey, descriptor, null);
		verify(mockView).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
		verify(mockView, Mockito.times(0)).configurePager(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void testPagerNotNecessary() throws JSONObjectAdapterException {
		testReturnJSONObject.put("totalNumberOfResults", 2);
		widget.configure(testWikiKey, descriptor, null);
		verify(mockView).configure(any(Map.class), any(String[].class), any(APITableInitializedColumnRenderer[].class), any(APITableConfig.class));
		verify(mockView, Mockito.times(0)).configurePager(anyInt(), anyInt(), anyInt());
	}
	
	@Test
	public void testPagingURI() throws JSONObjectAdapterException {
		widget.configure(testWikiKey, descriptor, null);
		String pagedURI = widget.getPagedURI();
		assertEquals(TESTSERVICE_PATH + "?limit=10&offset=0", pagedURI.toLowerCase());
	}
	
	@Test
	public void testQueryServicePagingURI() throws JSONObjectAdapterException {
		String testServiceCall = ClientProperties.QUERY_SERVICE_PREFIX+"select+*+from+project";
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, testServiceCall);
		widget.configure(testWikiKey, descriptor, null);
		String pagedURI = widget.getPagedURI();
		assertEquals(testServiceCall + "+limit+10+offset+1", pagedURI.toLowerCase());
	}

	
}
