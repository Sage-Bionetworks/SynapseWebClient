package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererSynapseID implements APITableColumnRenderer {

	private String outputColumnName;
	private Map<String, List<String>> outputColumnData;

	@Inject
	public APITableColumnRendererSynapseID() {
	}
	
	@Override
	public void init(Map<String, List<String>> columnData,
			APITableColumnConfig config,
			AsyncCallback<APITableInitializedColumnRenderer> callback) {
		outputColumnName = APITableWidget.getSingleOutputColumnName(config);
		
		//and precompute the output values
		outputColumnData = new HashMap<String, List<String>>();
		String inputColumnName = APITableWidget.getSingleInputColumnName(config);
		List<String> colValues = columnData.get(inputColumnName);
		if (colValues == null) {
			//user defined an input column that doesn't exist in the service output
			callback.onFailure(new IllegalArgumentException(DisplayConstants.ERROR_API_TABLE_RENDERER_MISSING_INPUT_COLUMN + inputColumnName));
			return;
		}
		List<String> outputValues = new ArrayList<String>();
		
		for (Iterator iterator2 = colValues.iterator(); iterator2
				.hasNext();) {
			String colValue = (String) iterator2.next();
			outputValues.add(getSynapseLinkHTML(colValue));
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
	
	public static String getSynapseLinkHTML(String value) {
		StringBuilder sb = new StringBuilder();
		sb.append("<a class=\"link\" href=\"");
		sb.append(DisplayUtils.getSynapseHistoryToken(value));
		sb.append("\">");
		sb.append(value);
		sb.append("</a>");
		return sb.toString();
	}
}
