package org.sagebionetworks.web.client.widget.team;

import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.web.client.SynapseView;
import com.google.gwt.user.client.ui.IsWidget;

public interface BigTeamBadgeView extends IsWidget, SynapseView {
	public void setTeam(Team team, String description);

	public void showLoadError(String principalId);

	public void setRequestCount(String count);

	void addStyleName(String style);

	void setHeight(String height);

	void setMemberCountWidget(IsWidget widget);

	void setTeamEmailAddress(String teamEmail);
}
