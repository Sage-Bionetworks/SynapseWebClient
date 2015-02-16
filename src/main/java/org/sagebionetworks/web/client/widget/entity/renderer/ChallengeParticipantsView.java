package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
public interface ChallengeParticipantsView extends IsWidget, SynapseView {
	
	interface Presenter {
	}

	/**
	 * Set the pagination widget
	 * @param string
	 */
	void setPaginationWidget(Widget paginationWidget);
	void clearParticipants();
	void addParticipant(UserProfile profile);
	void showNoParticipants();
	void hideErrors();
	void hideLoading();
	
	/**
	 * Bind this view to its presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
}
