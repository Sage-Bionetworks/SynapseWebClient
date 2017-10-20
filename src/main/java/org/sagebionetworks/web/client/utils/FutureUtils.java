package org.sagebionetworks.web.client.utils;

import java.util.function.Consumer;

import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class FutureUtils {
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
}
