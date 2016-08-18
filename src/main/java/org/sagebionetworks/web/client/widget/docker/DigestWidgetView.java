package org.sagebionetworks.web.client.widget.docker;

import com.google.gwt.user.client.ui.IsWidget;

public interface DigestWidgetView extends IsWidget{

	public interface Presenter {
	}

	void setDigest(String digest);
}
