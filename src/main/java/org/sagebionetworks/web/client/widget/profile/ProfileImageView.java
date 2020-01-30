package org.sagebionetworks.web.client.widget.profile;

import com.google.gwt.user.client.ui.IsWidget;

public interface ProfileImageView extends IsWidget {

	/**
	 * Show the default image.
	 */
	public void showDefault();

	/**
	 * Show the given URL.
	 * 
	 * @param url
	 */
	public void setImageUrl(String url);

	public void setRemovePictureButtonVisible(boolean isVisible);

	void setPresenter(ProfileImageWidget presenter);
}
