package org.sagebionetworks.web.client.widget.header;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface StuAnnouncementWidgetView extends IsWidget{

	public interface Presenter {
		Widget asWidget();
		void onClickAnnouncement();
	}

	void setPresenter(StuAnnouncementWidget presenter);
	void show(String text);
	void hide();
}
