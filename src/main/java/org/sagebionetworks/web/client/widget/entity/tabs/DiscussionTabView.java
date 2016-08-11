package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DiscussionTabView extends IsWidget{

	public interface Presenter {
	}

	void setPresenter(Presenter presenter);
	void showErrorMessage(String errorMessage);
	void setForum(Widget w);
	
	/**
	 * TODO: delete this method after taking the Discussion feature out of beta.
	 * @param t
	 */
	void updateWidth(Tab t);
}
