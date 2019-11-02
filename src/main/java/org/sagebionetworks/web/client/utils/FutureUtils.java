package org.sagebionetworks.web.client.utils;

import java.util.function.Consumer;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FutureUtils {
	/**
	 * Calls the given closure, passing it an AsyncCallback that transmits the success failure scenarios
	 * to a FluentFuture; then it returns the future.
	 *
	 * @param closure
	 * @param <T>
	 * @return a FluentFuture that represents the outcome of calling the closure
	 */
	public static <T> FluentFuture<T> getFuture(Consumer<AsyncCallback<T>> closure) {
		SettableFuture<T> future = SettableFuture.create();
		closure.accept(new AsyncCallback<T>() {
			@Override
			public void onFailure(Throwable caught) {
				future.setException(caught);
			}

			@Override
			public void onSuccess(T result) {
				future.set(result);
			}
		});
		return FluentFuture.from(future);
	}

	/**
	 * Returns a future that is already completed with the given value as the result.
	 *
	 * @param result
	 * @param <T>
	 * @return a future that is already completed with the given value as the result
	 */
	public static <T> FluentFuture<T> getDoneFuture(T result) {
		SettableFuture<T> future = SettableFuture.create();
		future.set(result);
		return FluentFuture.from(future);
	}

	/**
	 * Returns a failed future with its exception set to the given Throwable
	 *
	 * @return a failed future with its exception set to the given Throwable
	 */
	public static <T> FluentFuture<T> getFailedFuture(Throwable e) {
		SettableFuture<T> future = SettableFuture.create();
		future.setException(e);
		return FluentFuture.from(future);
	}

	/**
	 * Returns a failed future with its exception set to a new Throwable.
	 *
	 * @return a failed future with its exception set to a new Throwable
	 */
	public static <T> FluentFuture<T> getFailedFuture() {
		return getFailedFuture(new Throwable());
	}
}
