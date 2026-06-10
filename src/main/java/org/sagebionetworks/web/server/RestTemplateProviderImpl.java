package org.sagebionetworks.web.server;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.concurrent.TimeUnit;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * The purpose of this class it to setup the RestTemplate singleton in a thread-safe manner. Guice
 * will inject the configuration properties
 *
 * @see <a href="http://hc.apache.org/httpclient-3.x/threading.html">HttpClient threading</a>.
 *
 * @author jmhill
 *
 */
public class RestTemplateProviderImpl implements RestTemplateProvider {

  RestTemplate tempalteSingleton = null;

  /**
   * Injected via Guice from the ServerConstants.properties file.
   */
  @Inject
  public RestTemplateProviderImpl(
    @Named(
      "org.sagebionetworks.rest.template.connection.timout"
    ) int connectionTimeout,
    @Named(
      "org.sagebionetworks.rest.template.max.total.connections"
    ) int maxTotalConnections
  ) {
    PoolingHttpClientConnectionManager poolingManager =
      PoolingHttpClientConnectionManagerBuilder
        .create()
        .setMaxConnTotal(maxTotalConnections)
        .setDefaultConnectionConfig(
          ConnectionConfig
            .custom()
            .setSocketTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
            .build()
        )
        .build();
    HttpClient client = HttpClients
      .custom()
      .setConnectionManager(poolingManager)
      .build();
    HttpComponentsClientHttpRequestFactory factory =
      new HttpComponentsClientHttpRequestFactory(client);
    tempalteSingleton = new RestTemplate(factory);
  }

  @Override
  public RestTemplate getTemplate() {
    return tempalteSingleton;
  }
}
