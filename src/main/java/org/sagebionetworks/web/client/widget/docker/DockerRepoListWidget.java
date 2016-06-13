package org.sagebionetworks.web.client.widget.docker;

import org.sagebionetworks.web.client.widget.docker.modal.AddExternalRepoModal;
import org.sagebionetworks.web.client.widget.entity.controller.PreflightController;
import org.sagebionetworks.web.client.widget.pagination.PaginationWidget;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class DockerRepoListWidget implements DockerRepoListWidgetView.Presenter, DockerRepoAddedHandler {

	public static final Long PAGE_SIZE = 10L;
	public static final Long OFFSET_ZERO = 0L;

	private PreflightController preflightController;
	private DockerRepoListWidgetView view;
	private PaginationWidget paginationWidget;
	private AddExternalRepoModal addExternalRepoModal;

	@Inject
	public DockerRepoListWidget(
			PreflightController preflightController,
			DockerRepoListWidgetView view,
			PaginationWidget paginationWidget,
			AddExternalRepoModal addExternalRepoModal
			) {
		this.preflightController = preflightController;
		this.view = view;
		this.paginationWidget = paginationWidget;
		this.addExternalRepoModal = addExternalRepoModal;
		view.setPresenter(this);
		view.addPaginationWidget(paginationWidget);
		view.addExternalRepoModal(addExternalRepoModal.asWidget());
	}


	public Widget asWidget() {
		return view.asWidget();
	}

	@Override
	public void onClickAddExternalRepo() {
		addExternalRepoModal.show();
	}

	@Override
	public void repoAdded() {
		// TODO Auto-generated method stub
		
	}

}
