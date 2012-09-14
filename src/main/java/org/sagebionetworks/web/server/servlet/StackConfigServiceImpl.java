package org.sagebionetworks.web.server.servlet;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.web.client.StackConfigService;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
* 
* This class provides access to stack configuration information.
* It can be extended, as needed, with methods from StackConfiguration.
* 
* @author brucehoff
*
*/
public class StackConfigServiceImpl extends RemoteServiceServlet implements StackConfigService {
	
	public static final long serialVersionUID = 46893767375462651L;

	@Override
	public String getBCCSignupEnabled() {
		return StackConfiguration.getBCCSignupEnabled();
	}
	
	@Override
	public Integer getJiraGovernanceProjectId() {
		return StackConfiguration.getJiraGovernanceProjectId();
	}

}
