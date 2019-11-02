package org.sagebionetworks.web.client.widget.entity.renderer;

import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ChallengeTeamsView extends IsWidget, SynapseView {

	interface Presenter {
		/**
		 * edit button clicked on row
		 */
		void onEdit(String teamId);
	}

	/**
	 * Set the pagination widget
	 * 
	 * @param string
	 */
	void setPaginationWidget(Widget paginationWidget);

	void setEditRegisteredTeamDialog(Widget dialogWidget);

	void clearTeams();

	void addChallengeTeam(String teamId, String message, boolean showEditButton);

	void showNoTeams();

	void hideErrors();

	void hideLoading();

	/**
	 * Bind this view to its presenter.
	 * 
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
}
