package org.sagebionetworks.web.client;

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

	String getBCCSignupEnabled();
	
	Integer getJiraGovernanceProjectId();
}
