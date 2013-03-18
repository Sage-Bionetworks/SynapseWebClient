package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.repo.model.widget.APITableColumnConfig;

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
		outputColumnData.put(outputColumnName, columnData.get(APITableWidget.getSingleInputColumnName(config)));
		
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
