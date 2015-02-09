package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererLink;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableColumnRendererLinkTest {
		
	
	APITableColumnRendererLink renderer;
	SynapseJSNIUtils mockJsniUtils;
	GWTWrapper mockGwt;
	Map<String, List<String>> columnData;
	APITableColumnConfig config;
	AsyncCallback<APITableInitializedColumnRenderer> mockCallback;
	String inputColumnName = "links";
	String inputColumnValue = "[example](http://example.com/)";
	String expectedOutputColValue = "<a target=\"_blank\" href=\"http://example.com/\">example</a>";
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		mockJsniUtils = mock(SynapseJSNIUtils.class);
		mockGwt = mock(GWTWrapper.class);
		renderer = new APITableColumnRendererLink();
		columnData = new HashMap<String, List<String>>();
		config = new APITableColumnConfig();
		HashSet<String> inputColumnNames = new HashSet<String>();
		inputColumnNames.add(inputColumnName);
		config.setInputColumnNames(inputColumnNames);
		mockCallback = mock(AsyncCallback.class);
		APITableTestUtils.setInputValue(inputColumnValue, inputColumnName, columnData);
	}
	
	@Test
	public void testInitHappy() {
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		//output column name same as input
		assertTrue(initializedRenderer.getColumnData().containsKey(inputColumnName));
		assertEquals(expectedOutputColValue, initializedRenderer.getColumnData().get(inputColumnName).get(0));
	}
	
	@Test
	public void testInitNull() {
		APITableTestUtils.setInputValue(null, inputColumnName, columnData);
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		//null value should be rendered as an empty string
		assertEquals("", initializedRenderer.getColumnData().get(inputColumnName).get(0));
	}
	//Callback.onFailure is never called.  An empty column is shown if the data are unavailable for the specified column.
}
