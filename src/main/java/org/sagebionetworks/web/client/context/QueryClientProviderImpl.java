package org.sagebionetworks.web.client.context;

import javax.inject.Inject;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;
import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClientOptions;

public class QueryClientProviderImpl implements QueryClientProvider {

  private static QueryClient queryClientSingleton;

  @Inject
  public QueryClientProviderImpl() {
    if (queryClientSingleton == null) {
      queryClientSingleton = new QueryClient(QueryClientOptions.create());
      setQueryClient(queryClientSingleton);
    }
  }

  // Expose a method to store the queryClient in a global variable so we can access it from JSNI
  @JsProperty(namespace = JsPackage.GLOBAL, name = "SynapseQueryClient")
  static native void setQueryClient(QueryClient queryClient);

  @Override
  public QueryClient getQueryClient() {
    return queryClientSingleton;
  }
}
