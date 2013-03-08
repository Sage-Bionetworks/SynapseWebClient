package org.sagebionetworks.web.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.RequestBuilder.Method;

/**
 * wrapper for RequestBuilder so that it can be mocked
 * @author jayhodgson
 *
 */
public interface RequestBuilderWrapper {
	void configure(Method httpMethod, String url);
	Request sendRequest(String requestData, RequestCallback callback) throws RequestException;
}
