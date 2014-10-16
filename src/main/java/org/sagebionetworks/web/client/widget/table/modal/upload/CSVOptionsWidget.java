package org.sagebionetworks.web.client.widget.table.modal.upload;

import org.sagebionetworks.repo.model.table.UploadToTablePreviewRequest;
import org.sagebionetworks.repo.model.table.UploadToTableRequest;

import com.google.gwt.user.client.ui.IsWidget;
/**
 * Widget for managing CSV upload options.
 * @author jhill
 *
 */
public interface CSVOptionsWidget extends IsWidget {

	/**
	 * Configure this widget before using it.
	 * @param options The current options.
	 * @param handler Called when the options change.
	 */
	public void configure(UploadToTablePreviewRequest options, ChangeHandler handler);
	
	/**
	 * Get the current options for this widget.
	 * @return
	 */
	public UploadToTableRequest getCurrentOptions();
	
	/**
	 * Listener for option changes.
	 *
	 */
	public interface ChangeHandler{
		/**
		 * Will be called when the user changes any of the options.
		 */
		public void optionsChanged();
	}
}
