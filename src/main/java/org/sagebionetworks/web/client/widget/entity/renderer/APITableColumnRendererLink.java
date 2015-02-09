package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.markdown.constants.MarkdownRegExConstants;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererLink implements APITableColumnRenderer {

	private RegExp regExp = RegExp.compile(MarkdownRegExConstants.LINK_REGEX);
	private String outputColumnName;
	private Map<String, List<String>> outputColumnData;
	
	@Inject
	public APITableColumnRendererLink() {
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
			if (colValue == null) {
				outputValues.add("");
			} else {
				//try to find a match
				MatchResult matcher = regExp.exec(colValue);
				boolean matchFound = regExp.test(colValue);

				if (matchFound && matcher.getGroupCount() > 2) {
					String text = matcher.getGroup(2);
					String url = matcher.getGroup(3);
					StringBuilder output = new StringBuilder();
					output.append("<a target=\"_blank\" href=\"");
					output.append(url);
					output.append("\">");
					output.append(text);
					output.append("</a>");
					outputValues.add(output.toString());
				} else {
					//no match found
					outputValues.add(colValue);
				}
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
