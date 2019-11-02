package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.web.client.utils.Callback;
import com.google.gwt.user.client.ui.IsWidget;

public interface ProfileImageWidget extends IsWidget {

	/**
	 * Configure this widget before using.
	 * 
	 * @param userId
	 * @param fileHandleId
	 */
	public void configure(String userId, String fileHandleId);

	/**
	 * This method can be used to render a picture before it has been applied to the a user.
	 * 
	 * @param fileHandleId
	 */
	void configure(String fileHandleId);

	void setRemovePictureCallback(Callback callback);

	public void onRemovePicture();

}
