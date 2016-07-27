package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface BigTeamBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setTeam(Team team, String description, String xsrfToken);

	public void showLoadError(String principalId);
	
	public void setRequestCount(String count);
	
	void setTeamWithoutLink(String name);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
