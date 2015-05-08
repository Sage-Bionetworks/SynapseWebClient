package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;


/**
 * If a service call fails, then this will handle any standard error.  If unhandled, it will call back.
 *
 */
public interface ServiceErrorHandler extends IsWidget {
		
	public void onFailure(Throwable t, CallbackP<Throwable> unhandledErrorCallback);
}
