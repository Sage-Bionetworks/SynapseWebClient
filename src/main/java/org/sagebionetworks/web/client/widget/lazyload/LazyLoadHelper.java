package org.sagebionetworks.web.client.widget.lazyload;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.inject.Inject;

public class LazyLoadHelper {
	private Callback inViewportCallback;
	private SupportsLazyLoadInterface view;
	private Callback invokeCheckForInViewAndLoadData;
	boolean isAttached, isConfigured;
	LazyLoadCallbackQueue lazyLoadCallbackQueue;
	
	@Inject
	public LazyLoadHelper(
			LazyLoadCallbackQueue lazyLoadCallbackQueue
			) {
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
	
	public void checkForInViewAndLoadData() {
		if (!view.isAttached()) {
			//Done, view has been detached and widget was never in the viewport
			lazyLoadCallbackQueue.unsubscribe(invokeCheckForInViewAndLoadData);
			return;
		} else if (view.isInViewport()) {
			//try to load data!
			lazyLoadCallbackQueue.unsubscribe(invokeCheckForInViewAndLoadData);
			inViewportCallback.invoke();
		}
	}
	
	public void setIsConfigured() {
		this.isConfigured = true;
		startCheckingIfAttachedAndConfigured();
	}
}
