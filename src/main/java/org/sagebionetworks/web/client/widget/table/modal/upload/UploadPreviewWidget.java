package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.List;

import org.sagebionetworks.repo.model.table.ColumnModel;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for a widget that shows an upload preview.
 * 
 * @author John
 *
 */
public interface UploadPreviewWidget extends IsWidget{

	/**
	 * Configure this widget before using it.
	 * 
	 * @param preview
	 */
	public void configure(UploadToTablePreviewResult preview);
	
	/**
	 * Get the column models as they are currently configured.
	 * 
	 * @return
	 */
	public List<ColumnModel> getCurrentModel();
}
