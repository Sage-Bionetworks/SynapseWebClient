package org.sagebionetworks.web.server.servlet;

import com.google.gwt.thirdparty.guava.common.cache.CacheBuilder;
import com.google.gwt.thirdparty.guava.common.cache.CacheLoader;
import com.google.gwt.thirdparty.guava.common.cache.LoadingCache;
import com.google.inject.Inject;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.sagebionetworks.client.SynapseClient;
import org.sagebionetworks.client.exceptions.SynapseException;
import org.sagebionetworks.repo.model.versionInfo.SynapseVersionInfo;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class StackVersionProviderImpl implements StackVersionProvider {

  private static Log log = LogFactory.getLog(StackVersionProviderImpl.class);

  private final SynapseProvider synapseProvider;

  @Inject
  public StackVersionProviderImpl(SynapseProvider synapseProvider) {
    this.synapseProvider = synapseProvider;
  }

  private final CacheLoader<String, SynapseVersionInfo> versionCacheLoader =
    new CacheLoader<>() {
      @Override
      public SynapseVersionInfo load(String requestHost) {
        try {
          SynapseClient synapseClient = synapseProvider.createNewClient(
            requestHost
          );
          return synapseClient.getVersionInfo();
        } catch (SynapseException e) {
          log.error(e);
          return null;
        }
      }
    };

  private final LoadingCache<String, SynapseVersionInfo> synapseVersionCache =
    CacheBuilder
      .newBuilder()
      .maximumSize(50)
      .expireAfterWrite(5, TimeUnit.MINUTES)
      .build(versionCacheLoader);

  private SynapseVersionInfo getSynapseVersionInfo(String httpRequestHost) {
    try {
      return synapseVersionCache.get(httpRequestHost);
    } catch (ExecutionException e) {
      log.error(e);
      return null;
    }
  }

  @Override
  public String get(String httpRequestHost) throws RestServiceException {
    return (
      SynapseClientImpl.PortalVersionHolder.getVersionInfo() +
      "," +
      getSynapseVersionInfo(httpRequestHost).getVersion()
    );
  }
}
