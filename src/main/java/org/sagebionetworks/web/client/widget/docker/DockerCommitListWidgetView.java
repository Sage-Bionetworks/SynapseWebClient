package org.sagebionetworks.web.client.widget.docker;

import org.sagebionetworks.repo.model.docker.DockerCommit;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DockerCommitListWidgetView extends IsWidget {

	public interface Presenter {

		DockerCommit getCurrentCommit();
	}

	void setCommitsContainer(IsWidget widget);

	void setPresenter(Presenter presenter);

	void setSynAlert(Widget widget);

}
