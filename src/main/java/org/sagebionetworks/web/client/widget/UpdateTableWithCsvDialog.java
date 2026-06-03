package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.UpdateTableWithCsvDialogProps;

public interface UpdateTableWithCsvDialog extends IsWidget {
  void configure(
    String tableId,
    UpdateTableWithCsvDialogProps.OnSuccessFunction onSuccess,
    UpdateTableWithCsvDialogProps.OnCloseFunction onClose
  );
}
