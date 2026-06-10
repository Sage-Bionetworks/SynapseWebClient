package org.sagebionetworks.web.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestBuilder.Method;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import javax.inject.Inject;

public class RequestBuilderWrapperImpl implements RequestBuilderWrapper {

  @Inject
  public RequestBuilderWrapperImpl() {}

  RequestBuilder requestBuilder;

  @Override
  public void configure(Method httpMethod, String url) {
    requestBuilder = new RequestBuilder(httpMethod, url);
  }

  @Override
  public Request sendRequest(String requestData, RequestCallback callback)
    throws RequestException {
    return requestBuilder.sendRequest(requestData, callback);
  }

  @Override
  public void setHeader(String header, String value) {
    requestBuilder.setHeader(header, value);
  }
}
