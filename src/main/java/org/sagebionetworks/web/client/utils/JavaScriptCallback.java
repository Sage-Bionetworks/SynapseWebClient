package org.sagebionetworks.web.client.utils;

import com.google.gwt.core.client.JavaScriptObject;

/**
 * A simple interface for passing a method which is to be called by the recipient.
 * 
 * This is the same as a CallbackP<JavaScriptObject>. GWT was unable to resolve the invoke method
 * for the generic version: [ERROR] Referencing method
 * 'org.sagebionetworks.web.client.utils.CallbackP.invoke(Lcom/google/gwt/core/client/JavaScriptObject;)':
 * unable to resolve method
 *
 */
public interface JavaScriptCallback {
	void invoke(JavaScriptObject event);
}
