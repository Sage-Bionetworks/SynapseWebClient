package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;

public interface DownView extends IsWidget {
	void init();

	void setMessage(String message);

	void updateTimeToNextRefresh(int seconds);

	void setTimerVisible(boolean visible);

	boolean isAttached();
}
