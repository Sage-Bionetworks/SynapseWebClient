package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.List;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

/**
 * This is an optional last page for the CSV upload process. This page is used when the CSV is to be
 * used to append data to an existing table.
 * 
 * @author John
 * 
 */
public interface UploadCSVAppendPage extends ModalPage {

	/**
	 * Configure this widget before using.
	 * 
	 * @param request Data about how to read the CSV file.
	 * @param suggestedSchema The schema from the preview.
	 */
	public void configure(UploadToTableRequest request, List<ColumnModel> suggestedSchema);
}
