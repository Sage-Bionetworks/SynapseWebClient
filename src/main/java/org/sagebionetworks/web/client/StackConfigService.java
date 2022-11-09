package org.sagebionetworks.web.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import java.util.HashMap;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

/**
 * This interface allows the client to get stack configuration information.
 */
@RemoteServiceRelativePath("stackConfig")
public interface StackConfigService extends RemoteService {
  HashMap<String, String> getSynapseProperties() throws RestServiceException;
  StackStatus getCurrentStatus() throws RestServiceException;
}
