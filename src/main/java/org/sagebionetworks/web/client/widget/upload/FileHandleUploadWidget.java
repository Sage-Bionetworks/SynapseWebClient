package org.sagebionetworks.web.client.widget.upload;

import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * A one-click file handle upload widget.
 * 
 * @author John
 *
 */
public interface FileHandleUploadWidget extends IsWidget{

	/**
	 * Configure the widget before using.
	 * @param callback Will be called with a file handle Id if the user successfully uploads a file.
	 */
	public void configure(String buttonText, CallbackP<String> callback);
}
