package org.sagebionetworks.web.client.widget;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.CsvPreviewDialogProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class CsvPreviewDialogImpl
  extends ReactComponent
  implements CsvPreviewDialog {

  @Inject
  public CsvPreviewDialogImpl() {}

  @Override
  public void configure(
    String parentId,
    String tableId,
    CsvPreviewDialogProps.OnSuccessFunction onSuccess,
    CsvPreviewDialogProps.OnCloseFunction onClose
  ) {
    render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.CsvPreviewDialog,
        CsvPreviewDialogProps.create(
          true,
          parentId,
          tableId,
          () -> {
            close(parentId, tableId);
            onSuccess.onSuccess();
          },
          () -> {
            close(parentId, tableId);
            onClose.onClose();
          }
        )
      )
    );
  }

  private void close(String parentId, String tableId) {
    render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.CsvPreviewDialog,
        CsvPreviewDialogProps.create(false, parentId, tableId, null, () -> {})
      )
    );
  }
}
