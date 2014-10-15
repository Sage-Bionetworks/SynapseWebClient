package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;

public interface UploadCSVFinalPage extends ModalPage {
	
	public void configure(String fileName, String parentId, UploadToTableRequest request, List<ColumnModel> suggestedSchema);

}
