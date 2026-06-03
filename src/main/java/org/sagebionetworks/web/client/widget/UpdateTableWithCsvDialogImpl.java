package org.sagebionetworks.web.client.widget;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.UpdateTableWithCsvDialogProps;

public class UpdateTableWithCsvDialogImpl
  extends ReactComponent
  implements UpdateTableWithCsvDialog {

  @Inject
  public UpdateTableWithCsvDialogImpl() {}

  @Override
  public void configure(
    String tableId,
    UpdateTableWithCsvDialogProps.OnSuccessFunction onSuccess,
    UpdateTableWithCsvDialogProps.OnCloseFunction onClose
  ) {
    render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.UpdateTableWithCsvDialog,
        UpdateTableWithCsvDialogProps.create(
          true,
          tableId,
          () -> {
            close(tableId);
            onSuccess.onSuccess();
          },
          () -> {
            close(tableId);
            onClose.onClose();
          }
        )
      )
    );
  }

  private void close(String tableId) {
    render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.UpdateTableWithCsvDialog,
        UpdateTableWithCsvDialogProps.create(false, tableId, null, () -> {})
      )
    );
  }
}
