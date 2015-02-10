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
		List<String> colValues = APITableWidget.getColumnValues(inputColumnName, columnData);
		List<String> outputValues = new ArrayList<String>();
		
		for (String colValue : colValues) {
			if (colValue != null)
				outputValues.add(getSynapseLinkHTML(colValue));
			else
				outputValues.add("");
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
