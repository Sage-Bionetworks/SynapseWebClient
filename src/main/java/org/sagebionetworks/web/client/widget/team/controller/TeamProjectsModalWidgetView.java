package org.sagebionetworks.web.client.widget.team.controller;

import org.sagebionetworks.repo.model.ProjectListSortColumn;
import org.sagebionetworks.repo.model.entity.query.SortDirection;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface TeamProjectsModalWidgetView extends IsWidget {
	public interface Presenter {
		void sort(ProjectListSortColumn column);
	}

	void setSynAlertWidget(Widget asWidget);

	void setPresenter(Presenter presenter);

	void setTitle(String title);

	void show();

	void hide();

	void setProjectsContent(IsWidget projectsContent);

	void setSortDirection(ProjectListSortColumn column, SortDirection direction);
}
