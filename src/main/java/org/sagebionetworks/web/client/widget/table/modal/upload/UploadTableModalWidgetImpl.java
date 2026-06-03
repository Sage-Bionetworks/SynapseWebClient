package org.sagebionetworks.web.client.widget.table.modal.upload;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactElement;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.jsinterop.UploadCsvWizardProps;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalWizardWidget.WizardCallback;

/**
 * Renders the React {@code UploadCsvWizard} via JsInterop. Preserves the existing
 * {@link UploadTableModalWidget} contract so callers don't have to change.
 */
public class UploadTableModalWidgetImpl implements UploadTableModalWidget {

  private final ReactComponent reactComponent;

  private String parentId;
  private String tableId;
  private WizardCallback wizardCallback;

  @Inject
  public UploadTableModalWidgetImpl() {
    this.reactComponent = new ReactComponent();
  }

  @Override
  public void configure(String parentId, String tableId) {
    this.parentId = parentId;
    this.tableId = tableId;
    renderClosed();
  }

  @Override
  public void showModal(WizardCallback wizardCallback) {
    this.wizardCallback = wizardCallback;
    renderOpen();
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }

  private void renderClosed() {
    render(false);
  }

  private void renderOpen() {
    render(true);
  }

  private void render(boolean open) {
    UploadCsvWizardProps props = UploadCsvWizardProps.create(
      open,
      parentId,
      tableId,
      entityId -> handleComplete(),
      () -> handleClose()
    );
    ReactElement reactElement = React.createElementWithSynapseContext(
      SRC.SynapseComponents.UploadCsvWizard,
      props
    );
    reactComponent.render(reactElement);
  }

  private void handleComplete() {
    renderClosed();
    if (wizardCallback != null) {
      wizardCallback.onFinished();
    }
  }

  private void handleClose() {
    renderClosed();
    if (wizardCallback != null) {
      wizardCallback.onCanceled();
    }
  }
}
