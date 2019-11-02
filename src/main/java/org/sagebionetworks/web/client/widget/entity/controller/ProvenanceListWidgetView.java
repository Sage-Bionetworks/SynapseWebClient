package org.sagebionetworks.web.client.widget.entity.controller;

import java.util.List;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface ProvenanceListWidgetView {

	Widget asWidget();

	public interface Presenter {
		Widget asWidget();

		void addEntityRow();

		void addURLRow();

		void configure(List<ProvenanceEntry> provEntries);
	}

	void removeRow(IsWidget toRemove);

	void addRow(IsWidget newRow);

	void setPresenter(Presenter presenter);

	void setEntityFinder(IsWidget entityFinder);

	void setURLDialog(IsWidget urlDialog);

	void clear();

}
