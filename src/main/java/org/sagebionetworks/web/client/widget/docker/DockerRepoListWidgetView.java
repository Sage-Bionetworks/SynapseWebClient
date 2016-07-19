package org.sagebionetworks.web.client.widget.docker;

import java.util.List;

import org.sagebionetworks.repo.model.entity.query.EntityQueryResult;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface DockerRepoListWidgetView extends IsWidget {

	public interface Presenter {

		void onClickAddExternalRepo();

		void onRepoClicked(String id);
	}

	void addPaginationWidget(PaginationWidget paginationWidget);

	void showPaginationVisible(boolean visible);

	void setAddExternalRepoButtonVisible(boolean visibile);

	void addExternalRepoModal(IsWidget addExternalRepoModel);

	void setPresenter(Presenter presenter);

	void addRepos(List<EntityQueryResult> headers);

	void setSynAlert(Widget widget);

	void setSynAlertVisible(boolean visible);

	void clear();
}
