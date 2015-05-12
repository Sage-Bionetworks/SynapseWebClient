package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;


/**
 * If a service call fails, then this will handle displaying the error.  
 * Handle any specific errors that you know about before letting this widget handle. 
 *
 */
public interface SynapseAlert extends IsWidget {
	void handleException(Throwable t);
	void showError(String error);
	void clearState();
}
