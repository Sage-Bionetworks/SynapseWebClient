package org.sagebionetworks.web.client.widget.entity.dialog.editors;

import org.sagebionetworks.web.client.widget.WidgetDescriptorView;

import com.google.gwt.user.client.ui.IsWidget;

public interface YouTubeConfigView extends IsWidget, WidgetDescriptorView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void setVideoUrl(String url);
	public String getVideoUrl();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {

	}
}
