package org.sagebionetworks.web.client.widget.lazyload;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.inject.Inject;

public class LazyLoadHelper {
	private Callback inViewportCallback;
	private SupportsLazyLoadInterface view;
	private Callback invokeCheckForInViewAndLoadData;
	boolean isAttached, isConfigured;
	public LazyLoadCallbackQueue lazyLoadCallbackQueue;

	@Inject
	public LazyLoadHelper(LazyLoadCallbackQueue lazyLoadCallbackQueue) {
		this.lazyLoadCallbackQueue = lazyLoadCallbackQueue;
		isConfigured = false;
		isAttached = false;
		invokeCheckForInViewAndLoadData = new Callback() {
			@Override
			public void invoke() {
				checkForInViewAndLoadData();
			}
		};
	}

	public void setLazyLoadCallbackQueue(LazyLoadCallbackQueueImpl lazyLoadCallbackQueue) {
		this.lazyLoadCallbackQueue = lazyLoadCallbackQueue;
	}

	public void startCheckingIfAttachedAndConfigured() {
		if (isAttached && isConfigured) {
			lazyLoadCallbackQueue.subscribe(invokeCheckForInViewAndLoadData);
		}
	}

	public void configure(Callback inViewportCallback, SupportsLazyLoadInterface view) {
		this.inViewportCallback = inViewportCallback;
		this.view = view;

		view.setOnAttachCallback(new Callback() {
			@Override
			public void invoke() {
				isAttached = true;
				startCheckingIfAttachedAndConfigured();
			}
		});
	}

	private void clearAttachCallback() {
		view.setOnAttachCallback(() -> {
		});
	}

	public void checkForInViewAndLoadData() {
		if (!view.isAttached()) {
			// Done, view has been detached and widget was never in the viewport
			lazyLoadCallbackQueue.unsubscribe(invokeCheckForInViewAndLoadData);
			clearAttachCallback();
			return;
		} else if (view.isInViewport()) {
			// try to load data!
			lazyLoadCallbackQueue.unsubscribe(invokeCheckForInViewAndLoadData);
			clearAttachCallback();
			inViewportCallback.invoke();
		}
	}

	public void setIsConfigured() {
		this.isConfigured = true;
		startCheckingIfAttachedAndConfigured();
	}
}
