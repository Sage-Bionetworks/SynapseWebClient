package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.ShowsErrors;
import org.sagebionetworks.web.shared.ChallengeBundle;

import com.google.gwt.user.client.ui.IsWidget;

public interface ChallengeBadgeView extends IsWidget, ShowsErrors {
	void setChallenge(ChallengeBundle header);
	void setProjectId(String projectId);
}
