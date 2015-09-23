package org.sagebionetworks.web.client.widget.entity;

import org.sagebionetworks.web.client.SynapseView;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface EntityPageTopView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	void setProjectMetadata(Widget w);
	void setTabs(Widget w);
	void setActionMenu(Widget w);
	void setPageTitle(String title);
	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void refresh();

		void fireEntityUpdatedEvent();
	}
}
