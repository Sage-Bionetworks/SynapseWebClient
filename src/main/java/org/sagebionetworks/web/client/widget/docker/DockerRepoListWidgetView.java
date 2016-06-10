package org.sagebionetworks.web.client.widget.docker;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;

import com.google.gwt.user.client.ui.IsWidget;

public interface DockerRepoListWidgetView extends IsWidget, SynapseView {

	public interface Presenter {

		void onClickAddExternalRepo();
	}

	void addPaginationWidget(PaginationWidget paginationWidget);

	void showPaginationVisible(boolean visible);

	void setAddExternalRepoButtonVisible(boolean visibile);

	void addExternalRepoModal(IsWidget addExternalRepoModel);

	void addRepo(DockerRepoListGroupItem item);

	void setPresenter(Presenter presenter);
}
