package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.CsvPreviewDialogProps;

public interface CsvPreviewDialog extends IsWidget {
  void configure(
    String parentId,
    String tableId,
    CsvPreviewDialogProps.OnSuccessFunction onSuccess,
    CsvPreviewDialogProps.OnCloseFunction onClose
  );
}
