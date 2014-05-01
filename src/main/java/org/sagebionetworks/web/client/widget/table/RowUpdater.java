package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.repo.model.table.RowReferenceSet;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface RowUpdater {

	void updateRow(TableModel row, AsyncCallback<RowReferenceSet> callback);
}
