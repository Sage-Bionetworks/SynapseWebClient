package org.sagebionetworks.web.server.servlet;

import com.google.gwt.thirdparty.guava.common.base.Supplier;
import com.google.gwt.thirdparty.guava.common.base.Suppliers;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.status.StackStatus;
import org.sagebionetworks.web.client.StackConfigService;
import org.sagebionetworks.web.server.StackEndpoints;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalPropertiesHolder;
import org.sagebionetworks.web.server.servlet.SynapseClientImpl.PortalVersionHolder;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.ExceptionUtil;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

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

  public static final long serialVersionUID = 46893767375462651L;

  private final Supplier<SupplierCachedResult<StackStatus>> stackStatusCache =
    Suppliers.memoizeWithExpiration(
      stackStatusSupplier(),
      10,
      TimeUnit.SECONDS
    );

  public SupplierCachedResult<StackStatus> getStackStatus() {
    return stackStatusCache.get();
  }

  private Supplier<SupplierCachedResult<StackStatus>> stackStatusSupplier() {
    return new Supplier<SupplierCachedResult<StackStatus>>() {
      public SupplierCachedResult<StackStatus> get() {
        org.sagebionetworks.client.SynapseClient synapseClient =
          createAnonymousSynapseClient();
        try {
          SupplierCachedResult.success(synapseClient.getCurrentStackStatus());
        } catch (SynapseException e) {
          SupplierCachedResult.failure(e);
        }
        return null;
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
    SupplierCachedResult<StackStatus> currentStatus = getStackStatus();
    if (currentStatus.isSuccess()) {
      return currentStatus.getValue();
    } else {
      throw ExceptionUtil.convertSynapseException(currentStatus.getError());
    }
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
