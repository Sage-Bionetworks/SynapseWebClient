package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface IFrameView extends IsWidget {
	public void configure(String siteUrl, int height);
	public void showInvalidSiteUrl(String siteUrl);
}
