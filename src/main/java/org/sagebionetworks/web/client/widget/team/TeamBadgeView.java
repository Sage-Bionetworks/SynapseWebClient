package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;

public interface TeamBadgeView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void setTeam(Team team, Integer maxNameLength);

	public void showLoadError(String principalId);
	
	public void setRequestCount(String count);
	
	void setTeamWithoutLink(String name, String teamId);
	
	/**
	 * If a badge is made with the id, then a globe
	 * will be displayed as the profile picture.
	 * @param globeId
	 */
	void setGlobeId(Long globeId);
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
	}

}
