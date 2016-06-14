package org.sagebionetworks.web.client.widget.user;

import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface UserGroupListWidgetView extends IsWidget, SynapseView {
	/**
	 * Set this view's presenter
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	public void configure(List<UserGroupHeader> users);
	
	public interface Presenter {
		void goTo(Place place);
		void clear();
		Widget getBadgeWidget(String ownerId, boolean isIndividual, String displayName);
		boolean getIsBig();
	}
}
