package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.IsWidget;

public interface TeamBadgeView extends IsWidget, SynapseView {
	public void setTeam(Team team, Integer maxNameLength, String teamIconUrl, ClickHandler customClickHandler);

	public void showLoadError(String principalId);

	public void setRequestCount(String count);

	void setTeamWithoutLink(String name, boolean isPublic);

	void setVisible(boolean visible);

	void addStyleName(String style);

	void setTarget(String target);
}
