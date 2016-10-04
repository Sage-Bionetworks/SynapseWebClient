package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.shared.WidgetConstants.API_TABLE_COLUMN_RENDERER_CANCEL_REQUESTED;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererCancelRequested implements APITableColumnRenderer {
	private String outputColumnName;
	private Map<String, List<String>> outputColumnData;
	AuthenticationController authController;
	public final static String CANCEL_REQUEST_WIDGET_DIV_PREFIX = "cancel-requested-widget-";
	GWTWrapper gwt;
	//  This class simply adds elements that can be found later (to inject the CancelSubmissionWidget) after adding the table to the DOM.
	@Inject
	public APITableColumnRendererCancelRequested(AuthenticationController authController, GWTWrapper gwt) {
		this.authController = authController;
		this.gwt = gwt;
	}

	@Override
	public void init(final Map<String, List<String>> columnData,
			final APITableColumnConfig config,
			AsyncCallback<APITableInitializedColumnRenderer> callback) {
		if (!authController.isLoggedIn()) {
			return;
		}
		outputColumnName = APITableWidget.getSingleOutputColumnName(config);
		outputColumnData = new HashMap<String, List<String>>();
		List<String> cancelRequestedColumn = APITableWidget.getColumnValues(API_TABLE_COLUMN_RENDERER_CANCEL_REQUESTED, columnData);
		
		List<String> outputValues = new ArrayList<String>();
		if (cancelRequestedColumn == null) {
			//column unavailable
			callback.onFailure(new IllegalArgumentException(DisplayConstants.ERROR_API_TABLE_RENDERER_MISSING_INPUT_COLUMN + 
					API_TABLE_COLUMN_RENDERER_CANCEL_REQUESTED));
			return;
		} else {
			for (int i = 0; i < cancelRequestedColumn.size(); i++) {
				String uniqueId = gwt.getUniqueElementId();
				String cancelRequestedColValue = cancelRequestedColumn.get(i);
				// create div with an id that can be found later.
				String output = "<div id=\"" + CANCEL_REQUEST_WIDGET_DIV_PREFIX + uniqueId + "\" value=\"" + cancelRequestedColValue + "\" />";
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
