package org.sagebionetworks.web.client.widget.entity.tabs;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DockerTabView extends IsWidget {

	public interface Presenter {

	}

	void setPresenter(Presenter presenter);

	void setDockerRepoList(Widget widget);

	void setBreadcrumb(Widget widget);

	void setSynapseAlert(Widget widget);

	void setDockerRepoWidget(Widget widget);

	void setBreadcrumbVisible(boolean visible);

	void setDockerRepoListVisible(boolean visible);

	void setDockerRepoUIVisible(boolean visible);

	void clearDockerRepoWidget();
}
