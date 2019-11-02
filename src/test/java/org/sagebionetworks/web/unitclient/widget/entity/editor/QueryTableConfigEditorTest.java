package org.sagebionetworks.web.unitclient.widget.entity.editor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.schema.adapter.JSONArrayAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.JSONArrayAdapterImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.ClientProperties;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigEditor;
import org.sagebionetworks.web.client.widget.entity.editor.QueryTableConfigView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.WidgetConstants;
import org.sagebionetworks.web.shared.WikiPageKey;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class QueryTableConfigEditorTest {
	QueryTableConfigEditor editor;
	@Mock
	QueryTableConfigView mockView;
	WikiPageKey wikiKey = new WikiPageKey("", ObjectType.ENTITY.toString(), null);
	@Mock
	SynapseJavascriptClient mockSynapseJavascriptClient;
	@Mock
	WikiPageKey mockWikiKey;
	@Mock
	GWTWrapper mockGWT;
	JSONObjectAdapterImpl testReturnJSONObject;
	Map<String, String> descriptor;
	String testQuery = "select+*+from+table";
	String decodedTestQuery = "select * from table";
	String col1Name = WebConstants.DEFAULT_COL_NAME_CREATED_BY_PRINCIPAL_ID;
	String col2Name = WebConstants.DEFAULT_COL_NAME_MODIFIED_ON;

	@Before
	public void setup() throws JSONObjectAdapterException {
		MockitoAnnotations.initMocks(this);
		editor = new QueryTableConfigEditor(mockView, mockSynapseJavascriptClient, mockGWT);

		testReturnJSONObject = new JSONObjectAdapterImpl();
		testReturnJSONObject.put("totalNumberOfResults", 100);
		// and create some results
		JSONObjectAdapter result1 = testReturnJSONObject.createNew();
		fillInResult(result1, new String[] {col1Name, col2Name}, new String[] {"result1 value 1", "result1 value 2"});
		JSONObjectAdapter result2 = testReturnJSONObject.createNew();
		fillInResult(result2, new String[] {col1Name, col2Name}, new String[] {"result2 value 1", "result2 value 2"});
		JSONArrayAdapter results = new JSONArrayAdapterImpl();
		results.put(0, result2);
		results.put(0, result1);
		testReturnJSONObject.put("results", results);

		when(mockGWT.decodeQueryString(testQuery)).thenReturn(decodedTestQuery);
		when(mockGWT.encodeQueryString(decodedTestQuery)).thenReturn(testQuery);

		descriptor = new HashMap<String, String>();
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PATH_KEY, ClientProperties.QUERY_SERVICE_PREFIX + testQuery);
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY, "true");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY, "10");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_RESULTS_KEY, "results");
		descriptor.put(WidgetConstants.API_TABLE_WIDGET_CSS_STYLE, "myTableStyle");

		AsyncMockStubber.callSuccessWith(testReturnJSONObject).when(mockSynapseJavascriptClient).getJSON(anyString(), any(AsyncCallback.class));
		when(mockView.getQueryString()).thenReturn(testQuery);
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
		// check uri shown in view is the decoded query string
		assertEquals(decodedTestQuery, tableConfig.getUri());
	}

	@Test(expected = IllegalArgumentException.class)
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

		editor.updateDescriptorFromView();
		assertEquals(ClientProperties.QUERY_SERVICE_PREFIX + newQuery, descriptor.get(WidgetConstants.API_TABLE_WIDGET_PATH_KEY));
		assertEquals(isPaging.toString(), descriptor.get(WidgetConstants.API_TABLE_WIDGET_PAGING_KEY));
		assertEquals(QueryTableConfigEditor.DEFAULT_PAGE_SIZE, descriptor.get(WidgetConstants.API_TABLE_WIDGET_PAGESIZE_KEY));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testQueryWithOrderByAndPagination() {
		editor.configure(mockWikiKey, descriptor, null);
		decodedTestQuery = "select * from evaluation_1234 Order BY name";
		when(mockView.getQueryString()).thenReturn(decodedTestQuery);
		Boolean isPaging = true;
		when(mockView.isPaging()).thenReturn(isPaging);

		editor.updateDescriptorFromView();
	}


	@Test
	public void testAutoAddColumns() {
		editor.configure(mockWikiKey, descriptor, null);
		editor.autoAddColumns();
		verify(mockSynapseJavascriptClient).getJSON(anyString(), any(AsyncCallback.class));
		ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
		verify(mockView).setConfigs(captor.capture());
		List<APITableColumnConfig> configs = captor.getValue();
		assertEquals(2, configs.size());
		APITableColumnConfig columnConfig1 = configs.get(0);
		APITableColumnConfig columnConfig2 = configs.get(1);
		String columnConfig1Name = columnConfig1.getInputColumnNames().iterator().next();
		String columnConfig2Name = columnConfig2.getInputColumnNames().iterator().next();
		assertTrue(columnConfig1Name.equals(col1Name) || columnConfig1Name.equals(col2Name));
		assertTrue(columnConfig2Name.equals(col1Name) || columnConfig2Name.equals(col2Name));
		String columnConfig1Renderer = columnConfig1.getRendererFriendlyName();
		String columnConfig2Renderer = columnConfig2.getRendererFriendlyName();
		assertTrue(columnConfig1Renderer.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID) || columnConfig1Renderer.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE));
		assertTrue(columnConfig2Renderer.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID) || columnConfig2Renderer.equals(WidgetConstants.API_TABLE_COLUMN_RENDERER_EPOCH_DATE));
	}
}
