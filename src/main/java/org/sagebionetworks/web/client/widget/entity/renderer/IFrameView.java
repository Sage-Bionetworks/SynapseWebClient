package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.IsWidget;

public interface IFrameView extends IsWidget {
	void configure(String siteUrl, int height);

	void showError(String siteUrl);

	HandlerRegistration addAttachHandler(Handler handler);

	int getParentOffsetHeight();
}
