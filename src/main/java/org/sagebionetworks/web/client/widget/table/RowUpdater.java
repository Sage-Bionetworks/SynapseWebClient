package org.sagebionetworks.web.client.widget.table;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RowUpdater {

	void updateRow(TableModel row, AsyncCallback<Void> callback);
}
