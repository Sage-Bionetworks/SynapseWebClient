package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface SynapseForumView extends IsWidget{

	public interface Presenter {
	}

	void setPresenter(Presenter presenter);
	void setWikiWidget(Widget w);
	void setAlert(Widget w);
	void showErrorMessage(String errorMessage);
	void setForumWidget(Widget widget);
}
