package org.sagebionetworks.web.server.servlet;

import java.util.HashMap;

import org.sagebionetworks.StackConfiguration;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.repo.model.versionInfo.SynapseVersionInfo;
import org.sagebionetworks.web.client.StackConfigService;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalPropertiesHolder;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalVersionHolder;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

/**
* 
* This class provides access to stack configuration information.
* It can be extended, as needed, with methods from StackConfiguration.
* 
* @author brucehoff
*
*/
public class StackConfigServiceImpl extends SynapseClientBase implements StackConfigService {
	
	public static final long serialVersionUID = 46893767375462651L;

	@Override
	public String getDoiPrefix() {
		return StackConfiguration.getEzidDoiPrefix();
	}

	@Override
	public String getSynapseVersions() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createAnonymousSynapseClient();
		try {
			SynapseVersionInfo versionInfo = synapseClient.getVersionInfo();
			return PortalVersionHolder.getVersionInfo() + ","
					+ versionInfo.getVersion();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public StackStatus getCurrentStatus() throws RestServiceException {
		org.sagebionetworks.client.SynapseClient synapseClient = createAnonymousSynapseClient();
		try {
			return synapseClient.getCurrentStackStatus();
		} catch (Exception e) {
			throw new UnknownErrorException(e.getMessage());
		}
	}
	
	@Override
	public HashMap<String, String> getSynapseProperties(){
		HashMap<String, String> properties = PortalPropertiesHolder.getPropertiesMap();
		properties.put(WebConstants.REPO_SERVICE_URL_KEY, StackConfiguration.getRepositoryServiceEndpoint());
		properties.put(WebConstants.FILE_SERVICE_URL_KEY, StackConfiguration.getFileServiceEndpoint());
		properties.put(WebConstants.AUTH_PUBLIC_SERVICE_URL_KEY, StackConfiguration.getAuthenticationServicePublicEndpoint());
		properties.put(WebConstants.SYNAPSE_VERSION_KEY, PortalVersionHolder.getVersionInfo());
		return properties;
	}
}
