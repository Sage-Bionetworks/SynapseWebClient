package org.sagebionetworks.web.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

public class RequestBuilderWrapperImpl implements RequestBuilderWrapper {
	RequestBuilder requestBuilder;

	@Override
	public void configure(Method httpMethod, String url) {
		requestBuilder = new RequestBuilder(httpMethod, url);
	}

	@Override
	public Request sendRequest(String requestData, RequestCallback callback) throws RequestException {
		return requestBuilder.sendRequest(requestData, callback);
	}

	@Override
	public void setHeader(String header, String value) {
		requestBuilder.setHeader(header, value);
	}
}
