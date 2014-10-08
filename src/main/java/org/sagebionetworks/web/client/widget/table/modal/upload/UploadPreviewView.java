package org.sagebionetworks.web.client.widget.table.modal.upload;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;

/**
 * Abstraction for an upload preview.
 * 
 * @author John
 *
 */
public interface UploadPreviewView extends IsWidget{

	void setHeaders(List<String> headers);
	
	void addRow(List<String> row);

}
