package org.sagebionetworks.web.client.widget.team.controller;

import java.util.List;

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

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamProjectsModalWidget implements IsWidget, TeamProjectsModalWidgetView.Presenter {

	SynapseAlert synAlert;
	SynapseJavascriptClient jsClient;
	TeamProjectsModalWidgetView view;
	LoadMoreWidgetContainer loadMoreWidgetContainer;
	Team team;
	PortalGinInjector ginInjector;
	int currentOffset;
	Callback loadMoreCallback;
	ClickHandler projectBadgeClickHandler = event -> {
		view.hide();
	};
	ProjectListSortColumn currentSortColumn;
	SortDirection currentSortDirection;
	
	@Inject
	public TeamProjectsModalWidget(
			SynapseAlert synAlert,
			SynapseJavascriptClient jsClient,
			PortalGinInjector ginInjector,
			TeamProjectsModalWidgetView view) {
		this.synAlert = synAlert;
		this.jsClient = jsClient;
		this.ginInjector = ginInjector;
		this.view = view;
		loadMoreCallback = () -> {
			getMoreTeamProjects();
		};
		view.setPresenter(this);
		view.setSynAlertWidget(synAlert.asWidget());
	}
	
	@Override
	public Widget asWidget() {
		return view.asWidget();
	}

	public void clear() {
		synAlert.clear();
		loadMoreWidgetContainer = ginInjector.getLoadMoreProjectsWidgetContainer();
		loadMoreWidgetContainer.configure(loadMoreCallback);
		currentOffset = 0;
		view.setProjectsContent(loadMoreWidgetContainer);
	}
	
	public void configureAndShow(Team team) {
		clear();
		currentSortColumn = ProjectListSortColumn.LAST_ACTIVITY;
		currentSortDirection = SortDirection.DESC;
		this.team = team;
		String title = team.getName() + " Projects";
		view.setTitle(title);
		view.show();
		getMoreTeamProjects();
	}
	
	public void getMoreTeamProjects() {
		synAlert.clear();
		jsClient.getProjectsForTeam(team.getId(), ProfilePresenter.PROJECT_PAGE_SIZE, currentOffset, currentSortColumn, currentSortDirection, new AsyncCallback<List<ProjectHeader>>(){
			@Override
			public void onSuccess(List<ProjectHeader> projectHeaders) {
				for (int i = 0; i < projectHeaders.size(); i++) {
					ProjectBadge badge = ginInjector.getProjectBadgeWidget();
					badge.configure(projectHeaders.get(i));
					badge.addClickHandler(projectBadgeClickHandler);
					Widget widget = badge.asWidget();
					loadMoreWidgetContainer.add(widget);
				}
				
				currentOffset += ProfilePresenter.PROJECT_PAGE_SIZE;
				loadMoreWidgetContainer.setIsMore(projectHeaders.size() >= ProfilePresenter.PROJECT_PAGE_SIZE);
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	@Override
	public void sort(ProjectListSortColumn column) {
		clear();
		currentSortColumn = column;
		currentSortDirection = SortDirection.ASC.equals(currentSortDirection) ? SortDirection.DESC : SortDirection.ASC;
		view.setSortDirection(currentSortColumn, currentSortDirection);
		getMoreTeamProjects();
	}
}
