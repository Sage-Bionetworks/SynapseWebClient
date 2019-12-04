package org.sagebionetworks.web.client.widget.entity.renderer;

import com.google.gwt.user.client.ui.IsWidget;

public interface SRCDemoWidgetView extends IsWidget {
	void setSynAlertWidget(IsWidget w);

	void setPresenter(Presenter p);

	void setDemoVisible(boolean visible);

	void setLoadingVisible(boolean visible);

	void setLoadingMessage(String message);

	boolean isAttached();

	void newWindow(String url);

	public interface Presenter {
	}
}
