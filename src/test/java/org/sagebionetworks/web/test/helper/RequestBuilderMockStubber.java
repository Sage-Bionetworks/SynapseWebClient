package org.sagebionetworks.web.test.helper;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;

public class RequestBuilderMockStubber {
	public static Stubber callOnResponseReceived(final Request request, final Response response) {
		return Mockito.doAnswer(new Answer() {
			@Override
			@SuppressWarnings("unchecked")
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				final Object[] args = invocationOnMock.getArguments();
				((RequestCallback) args[args.length - 1]).onResponseReceived(request, response);
				return null;
			}
		});
	}

	public static Stubber callOnError(final Request request, final Throwable e) {
		return Mockito.doAnswer(new Answer() {
			@Override
			@SuppressWarnings("unchecked")
			public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
				final Object[] args = invocationOnMock.getArguments();
				((RequestCallback) args[args.length - 1]).onError(request, e);
				return null;
			}
		});
	}
}
