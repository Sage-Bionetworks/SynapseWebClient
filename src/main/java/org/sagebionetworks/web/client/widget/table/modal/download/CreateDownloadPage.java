package org.sagebionetworks.web.client.widget.table.modal.download;

import java.util.List;
import org.sagebionetworks.repo.model.table.FacetColumnRequest;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

/**
 * This is the first page allows the user to choose the options of the file to be created for
 * download.
 * 
 * @author John
 *
 */
public interface CreateDownloadPage extends ModalPage {

	/**
	 * Configure this widget before using it.
	 * 
	 * @param sql
	 */
	public void configure(String sql, String tableId, List<FacetColumnRequest> selectedFacets);

}
