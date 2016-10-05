package org.sagebionetworks.web.client.widget.entity.renderer;

import java.util.List;
import java.util.Map;

import org.sagebionetworks.web.client.SynapseView;
import org.sagebionetworks.web.client.widget.entity.editor.APITableColumnConfig;
import org.sagebionetworks.web.client.widget.entity.editor.APITableConfig;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface APITableWidgetView extends IsWidget, SynapseView {

	/**
	 * Set the presenter.
	 * @param presenter
	 */
	public void setPresenter(Presenter presenter);
	
	public void configure(Map<String, List<String>> columnData, String[] columnNames, APITableInitializedColumnRenderer[] renderers, APITableConfig tableConfig);
	/**
	 * Call to add the pager bar to the table
	 * @param start
	 * @param end
	 * @param total
	 */
	public void configurePager(int start, int end, int total);
	
	public void showError(Widget synAlert);
	
	void showTableUnavailable();
	
	/**
	 * Presenter interface
	 */
	public interface Presenter {
		void pageBack();
		void pageForward();
		void columnConfigClicked(APITableColumnConfig columnConfig);
		void refreshData();
	}
}
