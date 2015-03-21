package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererDate implements APITableColumnRenderer {

	private DateTimeFormat standardFormatter;
	private SynapseJSNIUtils synapseJSNIUtils;
	private String outputColumnName;
	private Map<String, List<String>> outputColumnData;
	
	@Inject
	public APITableColumnRendererDate(SynapseJSNIUtils synapseJSNIUtils, GWTWrapper gwt) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		standardFormatter = gwt.getDateTimeFormat(PredefinedFormat.ISO_8601);
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
		//assume dates are long values.  if parse exception occurs, then switch to standard formatter parsing (and don't try to parse as long again)
		boolean isMsFromEpoch = true;
		for (String colValue : colValues) {
			if (colValue == null) {
				outputValues.add("");
			} else {
				Date date = null;
				try {
					if (isMsFromEpoch)
						date = new Date(Long.parseLong(colValue));
					else
						date = standardFormatter.parse(colValue);	
				} catch (NumberFormatException e) {
					isMsFromEpoch = false;
					date = standardFormatter.parse(colValue);
				}
				outputValues.add(synapseJSNIUtils.convertDateToSmallString(date));
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
