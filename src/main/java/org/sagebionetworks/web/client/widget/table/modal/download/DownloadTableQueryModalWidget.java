package org.sagebionetworks.web.client.widget.table.modal.download;

import java.util.List;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * This widget is a modal dialog used to download the results of a table query.
 * 
 * @author jhill
 *
 */
public interface DownloadTableQueryModalWidget extends IsWidget {

	/**
	 * Configure this widget before using it.
	 * 
	 * @param sql The SQL to use to create the file download.
	 */
	public void configure(String sql, String tableId, List<FacetColumnRequest> selectedFacets);

	/**
	 * After configuring the widget call this method to show the dialog.
	 */
	public void showModal();

}
