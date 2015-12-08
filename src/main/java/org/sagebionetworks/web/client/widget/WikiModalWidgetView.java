package org.sagebionetworks.web.client.widget;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface WikiModalWidgetView extends SynapseView, IsWidget {
	void clear();
	void show();
	void hide();
	void setSynAlert(Widget w);
	void setWikiPage(Widget w);
	void setPresenter(Presenter presenter);
	void setTitle(String title);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}
}
