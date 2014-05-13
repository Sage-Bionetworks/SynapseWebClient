package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableColumnRendererNone implements APITableColumnRenderer {

	//does nothing
	private String outputColumnName;
	private Map<String, List<String>> outputColumnData;
	
	@Override
	public void init(final Map<String, List<String>> columnData,
			final APITableColumnConfig config,
			AsyncCallback<APITableInitializedColumnRenderer> callback) {
		outputColumnName = APITableWidget.getSingleOutputColumnName(config);
		
		outputColumnData = new HashMap<String, List<String>>();
		String inputColumnName = APITableWidget.getSingleInputColumnName(config);
		List<String> colValues = APITableWidget.getColumnValues(inputColumnName, columnData);
		List<String> outputValues = new ArrayList<String>();
		if (colValues == null) {
			//user defined an input column that doesn't exist in the service output
			callback.onFailure(new IllegalArgumentException(DisplayConstants.ERROR_API_TABLE_RENDERER_MISSING_INPUT_COLUMN + inputColumnName));
			return;
		} else {
			//replace null values with empty string
			for (String colValue : colValues) {
				if (colValue != null)
					outputValues.add(colValue);
				else
					outputValues.add("");
			}
		}
		outputColumnData.put(outputColumnName, outputValues);
		

		callback.onSuccess(new APITableInitializedColumnRenderer() {
			@Override
			public Map<String, List<String>> getColumnData() {
				return outputColumnData;
			}
			
			@Override
			public List<String> getColumnNames() {
				return APITableWidget.wrap(outputColumnName);
			}
		});
	}
}
