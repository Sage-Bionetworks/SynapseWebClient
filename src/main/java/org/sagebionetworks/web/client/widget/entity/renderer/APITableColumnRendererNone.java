package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class APITableColumnRendererNone implements APITableColumnRenderer {

	//does nothing
	@Override
	public void init(List<String> columnData, AsyncCallback<Void> callback) {
		callback.onSuccess(null);
	}
	@Override
	public int getColumnCount() {
		return 1;
	}
	@Override
	public String getRenderedColumnName(int rendererColIndex) {
		return null;
	}
	
	@Override
	public String render(String value, int rendererColIndex) {
		return value;
	}

}
