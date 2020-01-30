package org.sagebionetworks.web.client.widget.team.controller;

import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import org.sagebionetworks.web.client.widget.table.v2.results.SortableTableHeaderImpl;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TeamProjectsModalWidgetViewImpl implements TeamProjectsModalWidgetView {

	@UiField
	Modal modal;
	@UiField
	Button cancelButton;
	@UiField
	Div synAlertContainer;
	@UiField
	SortableTableHeaderImpl projectNameColumnHeader;
	@UiField
	SortableTableHeaderImpl lastActivityOnColumnHeader;
	@UiField
	Div projectsContainer;

	public interface Binder extends UiBinder<Widget, TeamProjectsModalWidgetViewImpl> {
	}

	Widget widget;
	Presenter presenter;

	@Inject
	public TeamProjectsModalWidgetViewImpl(Binder uiBinder) {
		this.widget = uiBinder.createAndBindUi(this);
		cancelButton.addClickHandler(event -> {
			modal.hide();
		});
		projectNameColumnHeader.setSortingListener(event -> {
			presenter.sort(ProjectListSortColumn.PROJECT_NAME);
		});
		lastActivityOnColumnHeader.setSortingListener(event -> {
			presenter.sort(ProjectListSortColumn.LAST_ACTIVITY);
		});
	}

	@Override
	public void setPresenter(Presenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public Widget asWidget() {
		return widget;
	}

	@Override
	public void setSynAlertWidget(Widget synAlert) {
		synAlertContainer.clear();
		synAlertContainer.add(synAlert);
	}

	@Override
	public void setProjectsContent(IsWidget projectsContent) {
		projectsContainer.clear();
		projectsContainer.add(projectsContent);
	}

	@Override
	public void show() {
		this.modal.show();
	}

	@Override
	public void hide() {
		this.modal.hide();
	}

	@Override
	public void setTitle(String title) {
		modal.setTitle(title);
	}

	@Override
	public void setSortDirection(ProjectListSortColumn column, SortDirection direction) {
		org.sagebionetworks.repo.model.table.SortDirection tableSortDirection = SortDirection.ASC.equals(direction) ? org.sagebionetworks.repo.model.table.SortDirection.ASC : org.sagebionetworks.repo.model.table.SortDirection.DESC;
		if (ProjectListSortColumn.PROJECT_NAME.equals(column)) {
			projectNameColumnHeader.setSortDirection(tableSortDirection);
			lastActivityOnColumnHeader.setSortDirection(null);
		} else {
			projectNameColumnHeader.setSortDirection(null);
			lastActivityOnColumnHeader.setSortDirection(tableSortDirection);
		}
	}
}
