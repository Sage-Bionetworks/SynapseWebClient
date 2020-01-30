package org.sagebionetworks.web.client.widget.table.modal.fileview;

import com.google.gwt.user.client.ui.IsWidget;

public interface EntityContainerListWidgetView extends IsWidget {
	void clear();

	void addEntity(String id, String name, boolean showDeleteButton);

	void setAddButtonVisible(boolean visible);

	void setSynAlert(IsWidget w);

	void setNoContainers(boolean visible);

	public interface Presenter {
		void onAddProject();

		void onRemoveProject(String id);
	}

	void setPresenter(Presenter presenter);

}
