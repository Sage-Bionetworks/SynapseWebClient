package org.sagebionetworks.web.client;

import java.util.HashMap;

import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * 
 * This interface allows the client to get stack configuration information.
 * It can be extended, as needed, with methods from StackConfiguration.
 * 
 * @author brucehoff
 *
 */
@RemoteServiceRelativePath("stackConfig")
public interface StackConfigService extends RemoteService {	

	String getDoiPrefix();
	String getSynapseVersions() throws RestServiceException;
	HashMap<String, String> getSynapseProperties() throws RestServiceException;
	StackStatus getCurrentStatus() throws RestServiceException;
}
