package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.Date;
import java.util.List;

import org.sagebionetworks.web.client.SynapseJSNIUtils;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class APITableColumnRendererDate implements APITableColumnRenderer {

	private DateTimeFormat formatter;
	private SynapseJSNIUtils synapseJSNIUtils;
	
	@Inject
	public APITableColumnRendererDate(SynapseJSNIUtils synapseJSNIUtils) {
		this.synapseJSNIUtils = synapseJSNIUtils;
		formatter = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);
	}
	
	//does nothing
	@Override
	public void init(List<String> columnData, AsyncCallback<APITableInitializedColumnRenderer> callback) {
		callback.onSuccess(new APITableInitializedColumnRenderer() {
			
			@Override
			public String render(String value, int rendererColIndex) {
				Date date = formatter.parse(value);
				return synapseJSNIUtils.convertDateToSmallString(date);
			}
			
			@Override
			public String getRenderedColumnName(int rendererColIndex) {
				return null;
			}
			
			@Override
			public int getColumnCount() {
				return 1;
			}
		});
	}
}
