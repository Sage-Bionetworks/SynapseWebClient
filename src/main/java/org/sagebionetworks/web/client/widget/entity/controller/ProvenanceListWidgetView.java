package org.sagebionetworks.web.client.widget.entity.controller;

import org.sagebionetworks.web.client.widget.entity.controller.ProvenanceListWidgetView.Presenter;

import com.google.gwt.user.client.ui.Widget;

public interface ProvenanceListWidgetView {

	Widget asWidget();
	
	public interface Presenter {
		Widget asWidget();

		void addEntityRow();

		void addURLRow();

		void loadEntityRow(String entityId);

		void loadURLRow(String title, String address);
	}

	void addRow(Widget newRow);

	void setPresenter(Presenter presenter);
}
