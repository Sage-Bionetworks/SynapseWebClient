package org.sagebionetworks.web.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TeamSearchView extends IsWidget {

	/**
	 * Set this view's presenter
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	public void setSearchTerm(String searchTerm);

	public interface Presenter {
		void goTo(Place place);
	}

	public void setSynAlertWidget(Widget asWidget);

	void setLoadMoreContainer(Widget w);
}
