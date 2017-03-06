package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.web.client.DataAccessClient;
import org.sagebionetworks.web.shared.PaginatedResults;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

@SuppressWarnings("serial")
public class DataAccessClientImpl extends SynapseClientBase implements DataAccessClient {

//	@Override
//	public ReturnType get() throws RestServiceException {
//		org.sagebionetworks.client.SynapseClient synapseClient = createSynapseClient();
//		try {
//			return synapseClient.domethod();
//		} catch (SynapseException e) {
//			throw ExceptionUtil.convertSynapseException(e);
//		}
//	}

}
