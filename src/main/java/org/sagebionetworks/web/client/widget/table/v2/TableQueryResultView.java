package org.sagebionetworks.web.client.widget.table.v2;

import org.sagebionetworks.web.client.view.bootstrap.table.TBody;
import org.sagebionetworks.web.client.view.bootstrap.table.THead;

public interface TableQueryResultView {

	public interface Presenter {
		
	}

	/**
	 * Bind the view to the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);

	/**
	 * Show the new table data.
	 * @param header
	 * @param body
	 */
	void configureTableData(THead header, TBody body);

	/**
	 * Show or hide the table.
	 * @param b
	 */
	void setTableVisible(boolean visible);
}
