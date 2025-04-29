package org.sagebionetworks.web.server.servlet;

import com.google.gwt.thirdparty.guava.common.base.Supplier;
import com.google.gwt.thirdparty.guava.common.base.Suppliers;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.web.client.StackConfigService;
import org.sagebionetworks.web.server.StackEndpoints;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalPropertiesHolder;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalVersionHolder;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.shared.exceptions.UnknownErrorException;

/**
 *
 * This class provides access to stack configuration information. It can be extended, as needed,
 * with methods from StackConfiguration.
 *
 * @author brucehoff
 *
 */
public class StackConfigServiceImpl
  extends SynapseClientBase
  implements StackConfigService {

  private static Log log = LogFactory.getLog(StackConfigServiceImpl.class);
  public static final long serialVersionUID = 46893767375462651L;

  private final Supplier<StackStatus> stackStatusCache =
    Suppliers.memoizeWithExpiration(
      stackStatusSupplier(),
      10,
      TimeUnit.SECONDS
    );

  public StackStatus getStackStatus() {
    return stackStatusCache.get();
  }

  private Supplier<StackStatus> stackStatusSupplier() {
    return new Supplier<StackStatus>() {
      public StackStatus get() {
        StackStatus status = null;
        org.sagebionetworks.client.SynapseClient synapseClient =
          createAnonymousSynapseClient();
        try {
          status = synapseClient.getCurrentStackStatus();
        } catch (Exception e) {
          log.error(e.getMessage());
        }
        return status;
      }
    };
  }

  @Override
  public String getRequestHost() {
    return UserDataProvider.getThreadLocalRequestHost(
      this.getThreadLocalRequest()
    );
  }

  @Override
  public StackStatus getCurrentStatus() throws RestServiceException {
    StackStatus currentStatus = getStackStatus();
    if (currentStatus == null) {
      throw new UnknownErrorException("Unable to get the current stack status");
    }
    return currentStatus;
  }

  @Override
  public HashMap<String, String> getSynapseProperties() {
    HashMap<String, String> properties =
      PortalPropertiesHolder.getPropertiesMap();
    properties.put(
      WebConstants.REPO_SERVICE_URL_KEY,
      StackEndpoints.getRepositoryServiceEndpoint(this.getRequestHost())
    );
    properties.put(
      WebConstants.FILE_SERVICE_URL_KEY,
      StackEndpoints.getFileServiceEndpoint(this.getRequestHost())
    );
    properties.put(
      WebConstants.AUTH_PUBLIC_SERVICE_URL_KEY,
      StackEndpoints.getAuthenticationServicePublicEndpoint(
        this.getRequestHost()
      )
    );
    properties.put(
      WebConstants.SYNAPSE_VERSION_KEY,
      PortalVersionHolder.getVersionInfo()
    );
    return properties;
  }
}
