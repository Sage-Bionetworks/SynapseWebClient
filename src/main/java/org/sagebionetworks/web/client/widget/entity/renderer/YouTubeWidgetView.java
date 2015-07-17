package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface YouTubeWidgetView extends IsWidget {

	
	public void configure(String videoId);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
