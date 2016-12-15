package org.sagebionetworks.web.client.widget.lazyload;

import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.utils.Callback;

import com.google.inject.Inject;

public class LazyLoadHelper {
	private Callback inViewportCallback;
	private SupportsLazyLoadInterface view;
	private GWTWrapper gwt;
	private Callback invokeCheckForInViewAndLoadData;
	boolean isAttached, isConfigured;
	
	@Inject
	public LazyLoadHelper(
			GWTWrapper gwt
			) {
		this.gwt = gwt;
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
			checkForInViewAndLoadData();
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
			return;
		} else if (view.isInViewport()) {
			//try to load data!
			inViewportCallback.invoke();
		} else {
			//wait for a few seconds and see if we should load data
			gwt.scheduleExecution(invokeCheckForInViewAndLoadData, DisplayConstants.DELAY_UNTIL_IN_VIEW);
		}
	}
	
	public void setIsConfigured() {
		this.isConfigured = true;
		startCheckingIfAttachedAndConfigured();
	}
}
