package org.sagebionetworks.web.client.context;

import org.sagebionetworks.web.client.jsinterop.reactquery.QueryClient;

/**
 * Provides a singleton react-query QueryClient.
 */
public interface QueryClientProvider {
  /**
   * Returns the global QueryClient
   */
  QueryClient getQueryClient();
}
