package org.sagebionetworks.web.client.widget.entity.controller;

import com.google.gwt.user.client.ui.Widget;

public interface ProvenanceListRowView {

	Widget asWidget();

	public interface Presenter {

		Widget asWidget();

		void configureAsEntity();

		void configureAsURL();
		
	}

	void setEntityFieldsVisible(boolean isVisible);
	
	void setURLFieldsVisible(boolean isVisible);

	void setEntityText(String entityId);

	void setURLName(String title);

	void setURLAddress(String address);

	void setEntityFinderWidget(Widget asWidget);

	void setItemText(String text);
}
