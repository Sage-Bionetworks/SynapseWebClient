package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.repo.model.EntityBundle;

import com.google.gwt.user.client.ui.Widget;

public interface ProvenanceEditorWidgetView {

	public interface Presenter {

		Widget asWidget();

		void configure(EntityBundle entityBundle);
		
	}

	Widget asWidget();

	void setVisible(boolean isVisible);

	void setSynAlertWidget(Widget asWidget);

	void setName(String name);

	void setDescription(String description);

	void setUsedProvenanceList(Widget widget);

	void setExecutedProvenanceList(Widget executedProvenanceList);
}
