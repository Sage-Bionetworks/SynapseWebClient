package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SynapseForumView extends IsWidget{

	public interface Presenter {
		void onClickNewThread();
		void onModeratorModeChange();
	}

	void setPresenter(Presenter presenter);
	void setThreadList(Widget w);
	void setWikiWidget(Widget w);
	void setNewThreadModal(Widget w);
	void setAlert(Widget w);
	void setModeratorModeContainerVisibility(Boolean visible);
	Boolean getModeratorMode();
	void showErrorMessage(String errorMessage);
}
