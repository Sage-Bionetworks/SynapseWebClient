package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.EntityBundle;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.Reference;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONArrayAdapterImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigView;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.VideoConfigView;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class QueryTableConfigEditorTest {
	QueryTableConfigEditor editor;
	@Mock
	QueryTableConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Mock
	SynapseClientAsync mockSynapseClient;
	@Mock
	JSONObjectAdapter mockJSONObjectAdapter;
	@Mock
	WikiPageKey mockWikiKey;
	@Mock
	GWTWrapper mockGWT;
	JSONObjectAdapterImpl testReturnJSONObject;
	Map<String, String> descriptor;
	String testJSON = "{totalNumberOfResults=10,results={}}";
	String testQuery = "select+*+from+table";
	String decodedTestQuery = "select * from table";
	@Before
	public void setup() throws JSONObjectAdapterException{
		MockitoAnnotations.initMocks(this);
		editor = new QueryTableConfigEditor(mockView, mockSynapseClient, mockJSONObjectAdapter, mockGWT);
		
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
		
		when(mockGWT.decodeQueryString(testQuery)).thenReturn(decodedTestQuery);
		when(mockGWT.encodeQueryString(decodedTestQuery)).thenReturn(testQuery);
		
		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, ClientProperties.QUERY_SERVICE_PREFIX + testQuery);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "true");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, "10");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY, "true");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_ROW_NUMBER_DISPLAY_NAME_KEY, "Row Number");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY, "results");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE, "myTableStyle");
		
		AsyncMockStubber.callSuccessWith(testJSON).when(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		
	}
	
	private void fillInResult(JSONObjectAdapter result, String[] fieldNames, String[] fieldValues) throws JSONObjectAdapterException {
		for (int i = 0; i < fieldNames.length; i++) {
			result.put(fieldNames[i], fieldValues[i]);
		}
	}
	
	@Test
	public void testAsWidget() {
		editor.asWidget();
		verify(mockView).asWidget();
	}
	
	
	@Test
	public void testConfigure() {
		editor.configure(mockWikiKey, descriptor, null);
		ArgumentCaptor<APITableConfig> captor = ArgumentCaptor.forClass(APITableConfig.class);
		verify(mockView).configure(captor.capture());
		APITableConfig tableConfig = captor.getValue();
		//check uri shown in view is the decoded query string
		assertEquals(decodedTestQuery, tableConfig.getUri());
	}
	
	@Test (expected=IllegalArgumentException.class)
	public void testUpdateDescriptorFromViewInvalidQuery() {
		when(mockView.getQueryString()).thenReturn("");
		editor.updateDescriptorFromView();
	}
	
	@Test
	public void testUpdateDescriptorFromView() {
		String newQuery = "select+name+from+project";
		when(mockGWT.encodeQueryString(anyString())).thenReturn(newQuery);
		editor.configure(mockWikiKey, descriptor, null);
		when(mockView.getQueryString()).thenReturn(decodedTestQuery);
		Boolean isPaging = true;
		when(mockView.isPaging()).thenReturn(isPaging);
		Boolean isShowRowNumbers = false;
		when(mockView.isShowRowNumbers()).thenReturn(isShowRowNumbers);
		
		editor.updateDescriptorFromView();
		assertEquals(ClientProperties.QUERY_SERVICE_PREFIX + newQuery,descriptor.get(WidgetConstants.API_TABLE_WIDGET_PATH_KEY));
		assertEquals(isPaging.toString(),descriptor.get(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY));
		assertEquals(QueryTableConfigEditor.DEFAULT_PAGE_SIZE,descriptor.get(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY));
		assertEquals(isShowRowNumbers.toString(),descriptor.get(WidgetConstants.API_TABLE_WIDGET_SHOW_ROW_NUMBER_KEY));
	}
	
	
	@Test
	public void testAutoAddColumns() {
		editor.configure(mockWikiKey, descriptor, null);
		editor.autoAddColumns();
		verify(mockSynapseClient).getJSONEntity(anyString(), any(AsyncCallback.class));
		
	}
}
