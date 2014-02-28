package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.table.QueryDetails;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface TableQueryUtilServiceAsync {

	void getQueryDetails(String query, AsyncCallback<QueryDetails> callback);

}
