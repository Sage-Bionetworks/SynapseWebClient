package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.List;
import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage;

public interface UploadCSVFinishPage extends ModalPage {
	/**
	 * Configure this widget before using it.
	 * 
	 * @param fileName
	 * @param parentId
	 * @param request
	 * @param suggestedSchema
	 */
	public void configure(String fileName, String parentId, UploadToTableRequest request, List<ColumnModel> suggestedSchema);

	/**
	 * Get the current state of the schema.
	 * 
	 * @return
	 */
	public List<ColumnModel> getCurrentSchema();

}
