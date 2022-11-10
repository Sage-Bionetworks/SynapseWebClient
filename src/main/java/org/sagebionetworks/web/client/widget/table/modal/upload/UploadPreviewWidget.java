package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.repo.model.table.UploadToTablePreviewResult;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;

/**
 * Abstraction for a widget that shows an upload preview.
 *
 * @author John
 *
 */
public interface UploadPreviewWidget extends IsWidget {
  /**
   * Configure this widget before using it.
   *
   * @param previewRequest
   *
   * @param preview
   * @throws JSONObjectAdapterException
   */
  public void configure(UploadToTablePreviewResult preview);
}
