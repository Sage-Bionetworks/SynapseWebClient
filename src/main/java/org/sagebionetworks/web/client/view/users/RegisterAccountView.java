package org.sagebionetworks.web.client.view.users;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface RegisterAccountView extends IsWidget {
	
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	void setRegisterWidget(Widget w);
	public interface Presenter {	
		void goTo(Place place);
	}
}
