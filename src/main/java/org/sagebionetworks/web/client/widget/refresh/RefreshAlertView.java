package org.sagebionetworks.web.client.widget.refresh;

import com.google.gwt.user.client.ui.IsWidget;

public interface RefreshAlertView extends IsWidget {

	void setPresenter(Presenter presenter);

	void setVisible(boolean visible);

	boolean isAttached();

	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void onRefresh();

		void onAttach();
	}

}
