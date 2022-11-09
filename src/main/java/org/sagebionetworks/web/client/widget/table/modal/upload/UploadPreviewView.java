package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.IsWidget;
import java.util.List;

/**
 * Abstraction for an upload preview.
 *
 * @author John
 *
 */
public interface UploadPreviewView extends IsWidget {
  void setHeaders(List<String> headers);

  void addRow(List<String> row);

  void setPreviewMessage(String string);

  void showEmptyPreviewMessage(String string);

  void setTableVisible(boolean visibile);

  void setEmptyMessageVisible(boolean visibile);
}
