package org.sagebionetworks.web.client.widget.biodalliance13;

import com.google.gwt.user.client.ui.IsWidget;

public interface BiodallianceWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	void setContainerId(String id);
	boolean isAttached();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void viewAttached();
	}
}
