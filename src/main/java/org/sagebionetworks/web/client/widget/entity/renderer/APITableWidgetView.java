package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;

import com.google.gwt.user.client.ui.IsWidget;

public interface APITableWidgetView extends IsWidget {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(Map<String, List<String>> columnData, String[] columnNames, String[] displayColumnNames, APITableColumnRenderer[] renderers, String tableWidth, boolean showRowNumbers, String rowNumberColName, String cssStyleName, int offset);
	/**
	 * Call to add the pager bar to the table
	 * @param start
	 * @param end
	 * @param total
	 */
	public void configurePager(int start, int end, int total);
	
	public void showError(String message);
	
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void pageBack();
		void pageForward();
	}
}
