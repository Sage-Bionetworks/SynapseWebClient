package org.sagebionetworks.web.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface PeopleSearchView extends IsWidget {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	void setSearchTerm(String searchTerm);

	void setLoadMoreContainer(Widget w);


	public interface Presenter {
		void goTo(Place place);
	}


	public void setSynAlertWidget(Widget asWidget);

	public void setSynAlertWidgetVisible(boolean b);
}
