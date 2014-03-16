
package org.sagebionetworks.web.client;

import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.table.QueryDetails;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("tablequeryutil")	
public interface TableQueryUtilService extends RemoteService {

	/**
	 * Returns a serialized QueryDetail object
	 * @param query
	 * @return
	 */
	public QueryDetails getQueryDetails(String query) throws RestServiceException; 
	
}
