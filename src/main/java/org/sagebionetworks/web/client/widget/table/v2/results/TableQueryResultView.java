package org.sagebionetworks.web.client.widget.table.v2.results;

import java.util.List;

import org.sagebionetworks.web.client.widget.table.v2.schema.ColumnTypeViewEnum;

public interface TableQueryResultView {

	public interface Presenter {
		
	}

	/**
	 * Bind the view to the presenter.
	 * @param presenter
	 */
	void setPresenter(Presenter presenter);
	
	/**
	 * 
	 * @param types
	 * @param rows
	 */
	void resetTableData(List<String> headers, List<ColumnTypeViewEnum> types, List<List<String>> rows);

	/**
	 * Show or hide the table.
	 * @param b
	 */
	void setTableVisible(boolean visible);
}
