package org.sagebionetworks.web.server.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.table.query.model.QuerySpecification;
import org.sagebionetworks.web.client.TableQueryUtilService;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.table.QueryDetails;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

@SuppressWarnings("serial")
public class TableQueryUtilServiceImpl extends RemoteServiceServlet implements TableQueryUtilService {
	static private Log log = LogFactory.getLog(TableQueryUtilServiceImpl.class);

	@Override
	public QueryDetails getQueryDetails(String query) throws RestServiceException {
		QueryDetails details = new QueryDetails();
		
		return details;
	}
}
