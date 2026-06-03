package org.sagebionetworks.web.client.widget;

import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.jsinterop.CreateTableFromCsvDialogProps;

public interface CreateTableFromCsvDialog extends IsWidget {
  void configure(
    String parentId,
    CreateTableFromCsvDialogProps.OnSuccessFunction onSuccess,
    CreateTableFromCsvDialogProps.OnCloseFunction onClose
  );
}
