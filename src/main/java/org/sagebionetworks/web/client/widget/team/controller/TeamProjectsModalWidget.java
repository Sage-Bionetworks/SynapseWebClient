package org.sagebionetworks.web.client.widget.team.controller;

import java.util.List;

import org.gwtbootstrap3.client.ui.ModalSize;
import org.sagebionetworks.repo.model.ProjectHeader;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.Team;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.presenter.ProfilePresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.ProjectBadge;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.modal.Dialog;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamProjectsModalWidget implements IsWidget {

	SynapseAlert synAlert;
	SynapseJavascriptClient jsClient;
	Dialog modal;
	LoadMoreWidgetContainer loadMoreWidgetContainer;
	Team team;
	PortalGinInjector ginInjector;
	int currentOffset;
	Callback loadMoreCallback;
	@Inject
	public TeamProjectsModalWidget(
			SynapseAlert synAlert,
			SynapseJavascriptClient jsClient,
			PortalGinInjector ginInjector,
			Dialog modal) {
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		this.ginInjector = ginInjector;
		this.modal = modal;
		loadMoreCallback = () -> {
			getMoreTeamProjects();
		};

	}
	
	@Override
	public Widget asWidget() {
		return modal.asWidget();
	}

	public void clear() {
		synAlert.clear();
		loadMoreWidgetContainer = ginInjector.getLoadMoreProjectsWidgetContainer();
		loadMoreWidgetContainer.configure(loadMoreCallback);
		currentOffset = 0;
	}
	
	public void configureAndShow(Team team) {
		clear();
		this.team = team;
		boolean autoHide = true;
		String title = team.getName() + " Projects";
		String defaultButtonText = "Close";
		String primaryButtonText = null;
		Dialog.Callback callback = null;
		modal.configure(title, loadMoreWidgetContainer.asWidget(), primaryButtonText, defaultButtonText, callback, autoHide);
		modal.setSize(ModalSize.LARGE);
		modal.show();
		getMoreTeamProjects();
	}
	
	public void getMoreTeamProjects() {
		jsClient.getProjectsForTeam(team.getId(), ProfilePresenter.PROJECT_PAGE_SIZE, currentOffset, ProjectListSortColumn.LAST_ACTIVITY, SortDirection.DESC, new AsyncCallback<List<ProjectHeader>>(){
			@Override
			public void onSuccess(List<ProjectHeader> projectHeaders) {
				for (int i = 0; i < projectHeaders.size(); i++) {
					ProjectBadge badge = ginInjector.getProjectBadgeWidget();
					badge.configure(projectHeaders.get(i));
					Widget widget = badge.asWidget();
					loadMoreWidgetContainer.add(widget);
				}
				
				currentOffset += ProfilePresenter.PROJECT_PAGE_SIZE;
				loadMoreWidgetContainer.setIsMore(projectHeaders.size() >= ProfilePresenter.PROJECT_PAGE_SIZE);
			}
			@Override
			public void onFailure(Throwable caught) {
				// TODO: show error
			}
		});
	}
}
