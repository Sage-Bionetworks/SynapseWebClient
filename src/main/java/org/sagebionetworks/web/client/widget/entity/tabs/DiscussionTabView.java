package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionTabView extends IsWidget{

	public interface Presenter {
		void onClickNewThread();

		void onModeratorModeChange();
		void onClickShowAllThreads();
	}

	void setPresenter(Presenter presenter);
	void setThreadList(Widget w);
	void setSingleThread(Widget w);
	void setNewThreadModal(Widget w);
	void setAlert(Widget w);
	void setModeratorModeContainerVisibility(Boolean visible);
	Boolean getModeratorMode();
	void showErrorMessage(String errorMessage);
	void setSingleThreadUIVisible(boolean visible);
	void setThreadListUIVisible(boolean visible);
}
