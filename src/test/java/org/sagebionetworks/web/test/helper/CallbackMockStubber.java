package org.sagebionetworks.web.test.helper;

import static org.mockito.Mockito.doAnswer;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.sagebionetworks.web.client.utils.Callback;

public class CallbackMockStubber {
	public static Stubber invokeCallback() {
		return doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				final Object[] args = invocation.getArguments();
				for (Object arg : args) {
					if (arg instanceof Callback) {
						((Callback) arg).invoke();
						return null;
					}
				}
				return null;
			}
		});
	}
}
