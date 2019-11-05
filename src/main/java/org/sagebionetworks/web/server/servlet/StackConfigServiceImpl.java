package org.sagebionetworks.web.server.servlet;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.repo.model.versionInfo.SynapseVersionInfo;
import org.sagebionetworks.web.client.StackConfigService;
import org.sagebionetworks.web.client.StackEndpoints;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalPropertiesHolder;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalVersionHolder;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;
import com.google.gwt.thirdparty.guava.common.base.Supplier;
import com.google.gwt.thirdparty.guava.common.base.Suppliers;

/**
 * 
 * This class provides access to stack configuration information. It can be extended, as needed,
 * with methods from StackConfiguration.
 * 
 * @author brucehoff
 *
 */
public class StackConfigServiceImpl extends SynapseClientBase implements StackConfigService {
	static private Log log = LogFactory.getLog(StackConfigServiceImpl.class);
	public static final long serialVersionUID = 46893767375462651L;

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
	public HashMap<String, String> getSynapseProperties() {
		HashMap<String, String> properties = PortalPropertiesHolder.getPropertiesMap();
		properties.put(WebConstants.REPO_SERVICE_URL_KEY, StackEndpoints.getRepositoryServiceEndpoint());
		properties.put(WebConstants.FILE_SERVICE_URL_KEY, StackEndpoints.getFileServiceEndpoint());
		properties.put(WebConstants.AUTH_PUBLIC_SERVICE_URL_KEY, StackEndpoints.getAuthenticationServicePublicEndpoint());
		properties.put(WebConstants.SYNAPSE_VERSION_KEY, PortalVersionHolder.getVersionInfo());
		return properties;
	}
}
