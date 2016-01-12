package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionTabView extends IsWidget{

	public interface Presenter {
		void onClickNewThread();
	}

	void setPresenter(Presenter presenter);
	void setDiscussionList(Widget w);
	void setNewThreadModal(Widget w);
	void setAlert(Widget w);
}
