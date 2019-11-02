package org.sagebionetworks.web.test.helper;

import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.Stubber;
import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class AsyncMockStubber {

	/**
	 * Create an answer that will call AsyncCallback.onSuccess();
	 * 
	 * @param data
	 * @return
	 */
	public static <T> Answer<T> createSuccessAnswer(final T data) {
		return new Answer<T>() {
			@Override
			@SuppressWarnings("unchecked")
			public T answer(InvocationOnMock invocationOnMock) throws Throwable {
				final Object[] args = invocationOnMock.getArguments();
				((AsyncCallback<T>) args[args.length - 1]).onSuccess(data);
				return null;
			}
		};
	}

	/**
	 * The answer will call Callback.invoke();
	 * 
	 * @return
	 */
	public static Answer<Void> createInvokeAnswer() {
		return new Answer<Void>() {
			@Override
			@SuppressWarnings("unchecked")
			public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
				final Object[] args = invocationOnMock.getArguments();
				((Callback) args[args.length - 1]).invoke();
				return null;
			}
		};
	}

	/**
	 * Create an Answer that will call AsyncCallback.onFailure();
	 * 
	 * @param caught
	 * @return
	 */
	public static <T extends Throwable> Answer<T> createFailedAnswer(final T caught) {
		return new Answer<T>() {
			@Override
			@SuppressWarnings("unchecked")
			public T answer(InvocationOnMock invocationOnMock) throws Throwable {
				final Object[] args = invocationOnMock.getArguments();
				((AsyncCallback<T>) args[args.length - 1]).onFailure(caught);
				return null;
			}
		};
	}

	/**
	 * The resulting stubber will call the AsyncCallback.onSuccess() method in sequence for each
	 * provided value.
	 * 
	 * @param dataArray
	 * @return
	 */
	public static <T> Stubber callSuccessWith(final T... dataArray) {
		if (dataArray == null || dataArray.length < 1) {
			// handle null
			return Mockito.doAnswer(createSuccessAnswer(null));
		}
		// each answer will be chained to the resulting stubber.
		Stubber last = null;
		// The rest are chained to this stubber
		for (T data : dataArray) {
			if (last == null) {
				// Start the chain
				last = Mockito.doAnswer(createSuccessAnswer(data));
			} else {
				// extend the chain.
				last = last.doAnswer(createSuccessAnswer(data));
			}
		}
		return last;
	}

	/**
	 * The resulting stubber will call the AsyncCallback.onFailure() method in sequence for each
	 * provided value.
	 * 
	 * @param caughtArray
	 * @return
	 */
	public static <T extends Throwable> Stubber callFailureWith(final T... caughtArray) {
		if (caughtArray == null || caughtArray.length < 1) {
			// handle null
			return Mockito.doAnswer(createFailedAnswer(null));
		}
		// each answer will be chained to the resulting stubber.
		Stubber last = null;
		// The rest are chained to this stubber
		for (T caught : caughtArray) {
			if (last == null) {
				// Start the chain
				last = Mockito.doAnswer(createFailedAnswer(caught));
			} else {
				// extend the chain.
				last = last.doAnswer(createFailedAnswer(caught));
			}
		}
		return last;
	}

	/**
	 * The resulting stubber will call the AsyncCallback.onFailure() for each exceptions, and
	 * Asynch.onSuccess() for each non-exception.
	 * 
	 * @param dataArray
	 * @return
	 */
	public static <T> Stubber callMixedWith(final T... dataArray) {
		if (dataArray == null || dataArray.length < 1) {
			// handle null
			throw new IllegalArgumentException("input cannot be null");
		}
		// each answer will be chained to the resulting stubber.
		Stubber last = null;
		// The rest are chained to this stubber
		for (T data : dataArray) {
			if (last == null) {
				if (data instanceof Throwable) {
					// Start the chain with failure
					last = Mockito.doAnswer(createFailedAnswer((Throwable) data));
				} else {
					// Start the chain with success.
					last = Mockito.doAnswer(createSuccessAnswer(data));
				}
			} else {
				if (data instanceof Throwable) {
					// extend the chain with a failure
					last = last.doAnswer(createFailedAnswer((Throwable) data));
				} else {
					// extend the chain with success
					last = last.doAnswer(createSuccessAnswer(data));
				}
			}
		}
		return last;
	}

	/**
	 * Create a stubber that will call invoke() on the last callback of any method.
	 * 
	 * @param data
	 * @return
	 */
	public static Stubber callWithInvoke() {
		return Mockito.doAnswer(createInvokeAnswer());
	}

	/**
	 * Creates a stubber that will not call invoke on the last callback of any method. This is not
	 * strictly necessary but it allows for symmetry for test that do invoke.
	 * 
	 * @return
	 */
	public static Stubber callNoInvovke() {
		return Mockito.doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				// nothing to do here
				return null;
			}
		});
	}
}
