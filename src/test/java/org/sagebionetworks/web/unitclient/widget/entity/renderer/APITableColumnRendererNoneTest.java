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
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableColumnRendererNone;
import org.sagebionetworks.web.client.widget.entity.renderer.APITableInitializedColumnRenderer;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableColumnRendererNoneTest {
		
	
	APITableColumnRendererNone renderer;
	Map<String, List<String>> columnData;
	APITableColumnConfig config;
	AsyncCallback<APITableInitializedColumnRenderer> mockCallback;
	String inputColumnName = "name";
	String inputValue = "original";
	String outputValue = inputValue;
	
	@Before
	public void setup() throws JSONObjectAdapterException{
		renderer = new APITableColumnRendererNone();
		columnData = new HashMap<String, List<String>>();
		config = new APITableColumnConfig();
		HashSet<String> inputColumnNames = new HashSet<String>();
		inputColumnNames.add(inputColumnName);
		config.setInputColumnNames(inputColumnNames);
		mockCallback = mock(AsyncCallback.class);
		APITableTestUtils.setInputValue(inputValue, inputColumnName, columnData);
	}
	
	@Test
	public void testInitHappy() {
		renderer.init(columnData, config, mockCallback);
		APITableInitializedColumnRenderer initializedRenderer = APITableTestUtils.getInitializedRenderer(mockCallback);
		//output column name same as input
		assertTrue(initializedRenderer.getColumnData().containsKey(inputColumnName));
		assertEquals(outputValue, initializedRenderer.getColumnData().get(inputColumnName).get(0));
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
	
	/**  NumberFormat is gwt client specific, so testDecimalNumberFormat and testGetColumnValue are in GwtTestSuite **/
}
