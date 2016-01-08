package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;

public interface DiscussionTabView extends IsWidget{
	void setPresenter(Presenter presenter);

	public interface Presenter {
		void onClickNewThread();
	}
}
