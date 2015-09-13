package org.sagebionetworks.web.client.widget.profile;

import org.sagebionetworks.web.client.utils.Callback;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProfileImageView extends IsWidget{

	/**
	 * Show the default image.
	 */
	public void showDefault();
	
	/**
	 * Show the given URL.
	 * @param url
	 */
	public void setImageUrl(String url);

	void setRemovePictureCallback(Callback callback);

	public void setRemovePictureButtonVisible(boolean isVisible);
}
