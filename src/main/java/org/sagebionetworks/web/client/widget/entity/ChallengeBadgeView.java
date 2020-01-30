package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.ShowsErrors;
import com.google.gwt.user.client.ui.IsWidget;

public interface ChallengeBadgeView extends IsWidget, ShowsErrors {
	void setProjectName(String projectName);

	void setProjectId(String projectId);
}
