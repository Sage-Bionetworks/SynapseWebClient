package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_COLUMN_RENDERER_USER_ID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererUserId implements APITableColumnRenderer {
	String outputColumnName;
	private Map<String, List<String>> outputColumnData;
	GWTWrapper gwt;
	public final static String USER_WIDGET_DIV_PREFIX = "user-widget-";
	@Inject
	public APITableColumnRendererUserId(GWTWrapper gwt) {
		this.gwt = gwt;
	}
	
	@Override
	public void init(Map<String, List<String>> columnData,
			APITableColumnConfig config,
			final AsyncCallback<APITableInitializedColumnRenderer> callback) {
		String inputColumnName = APITableWidget.getSingleInputColumnName(config);
		outputColumnName = APITableWidget.getSingleOutputColumnName(config);
		outputColumnData = new HashMap<String, List<String>>();
		List<String> column = APITableWidget.getColumnValues(inputColumnName, columnData);
		List<String> outputValues = new ArrayList<String>();
		if (column == null) {
			//column unavailable
			callback.onFailure(new IllegalArgumentException(DisplayConstants.ERROR_API_TABLE_RENDERER_MISSING_INPUT_COLUMN + 
					API_TABLE_COLUMN_RENDERER_USER_ID));
			return;
		} else {
			for (int i = 0; i < column.size(); i++) {
				String uniqueId = gwt.getUniqueElementId();
				String colValue = column.get(i);
				// create div with an id that can be found later.
				String output;
				if (colValue != null) {
					 output = "<div class=\"min-width-200 text-align-left\" id=\"" + USER_WIDGET_DIV_PREFIX + uniqueId + "\" value=\"" + colValue + "\" />";	
				} else {
					output = "";
				}
				
				outputValues.add(output);
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
