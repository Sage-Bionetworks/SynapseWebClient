package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ProvenanceEditorWidgetView {

	public interface Presenter {

		Widget asWidget();

		void onSave();

		void clear();
	}

	Widget asWidget();

	void setSynAlertWidget(IsWidget asWidget);

	void setName(String name);

	void setDescription(String description);

	void setUsedProvenanceList(IsWidget widget);

	void setExecutedProvenanceList(IsWidget executedProvenanceList);

	void setPresenter(Presenter presenter);

	void clear();

	void hide();

	void show();

	String getDescription();

	String getName();

	void setEntityFinder(IsWidget entityFinder);

	void setURLDialog(IsWidget urlDialog);
}
