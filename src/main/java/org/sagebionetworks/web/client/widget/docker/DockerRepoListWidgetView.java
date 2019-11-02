package org.sagebionetworks.web.client.widget.docker;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.docker.DockerRepository;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DockerRepoListWidgetView extends IsWidget {
	void setSynAlert(Widget widget);

	void setSynAlertVisible(boolean visible);

	void clear();

	void addRepo(EntityHeader entity);

	void setDockerRepository(DockerRepository entity);

	void setMembersContainer(LoadMoreWidgetContainer membersContainer);

	void setEntityClickedHandler(CallbackP<String> entityClickedHandler);

	void setLoadingVisible(boolean visible);
}
