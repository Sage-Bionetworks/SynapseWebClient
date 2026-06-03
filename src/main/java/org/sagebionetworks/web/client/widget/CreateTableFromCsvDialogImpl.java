package org.sagebionetworks.web.client.widget;

import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.CreateTableFromCsvDialogProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class CreateTableFromCsvDialogImpl
  extends ReactComponent
  implements CreateTableFromCsvDialog {

  @Inject
  public CreateTableFromCsvDialogImpl() {}

  @Override
  public void configure(
    String parentId,
    CreateTableFromCsvDialogProps.OnSuccessFunction onSuccess,
    CreateTableFromCsvDialogProps.OnCloseFunction onClose
  ) {
    render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.CreateTableFromCsvDialog,
        CreateTableFromCsvDialogProps.create(
          true,
          parentId,
          () -> {
            close(parentId);
            onSuccess.onSuccess();
          },
          () -> {
            close(parentId);
            onClose.onClose();
          }
        )
      )
    );
  }

  private void close(String parentId) {
    render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.CreateTableFromCsvDialog,
        CreateTableFromCsvDialogProps.create(false, parentId, null, () -> {})
      )
    );
  }
}
