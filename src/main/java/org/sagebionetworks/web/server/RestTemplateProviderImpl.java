package org.sagebionetworks.web.server;

import org.apache.http.client.HttpClient;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import com.google.inject.Inject;
import com.google.inject.name.Named;

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
	public RestTemplateProviderImpl(@Named("org.sagebionetworks.rest.template.connection.timout") int connectionTimeout, @Named("org.sagebionetworks.rest.template.max.total.connections") int maxTotalConnections) {

		// This connection manager allows us to have multiple thread
		// making http calls.
		// For now use the default values.
		PoolingHttpClientConnectionManager poolingManager = new PoolingHttpClientConnectionManager();
		poolingManager.setMaxTotal(maxTotalConnections);
		HttpClientBuilder builder = HttpClientBuilder.create();
		builder.setConnectionManager(poolingManager);
		builder.setDefaultSocketConfig(SocketConfig.custom().setSoTimeout(connectionTimeout).build());
		HttpClient client = builder.build();
		// We can now use this manager to create our rest template
		HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(client);
		tempalteSingleton = new RestTemplate(factory);
	}

	@Override
	public RestTemplate getTemplate() {
		return tempalteSingleton;
	}

}
