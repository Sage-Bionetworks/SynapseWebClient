package org.sagebionetworks.web.client.widget.table;

import java.util.List;

import org.sagebionetworks.repo.model.BatchResults;
import org.sagebionetworks.repo.model.table.ColumnModel;
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

	public void configure(TableObject table, List<ColumnModel> columns, String queryString, boolean canEdit);
	
	
}
