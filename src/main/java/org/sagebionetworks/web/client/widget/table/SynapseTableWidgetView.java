package org.sagebionetworks.web.client.widget.table;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.shared.TableObject;

import com.google.gwt.user.client.ui.IsWidget;

public interface SynapseTableWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);

	/**
	 * Presenter interface
	 */
	public interface Presenter {

		void query(String query);
				
	}

	public void configure(TableObject table, String queryString);
	
	
}
