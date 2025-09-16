package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import elemental2.dom.DomGlobal;
import elemental2.dom.FileList;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.jsinterop.EntityUploadHandle;
import org.sagebionetworks.web.client.jsinterop.EntityUploadModalProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactRefObject;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class UploadDialogWidgetV2 extends Widget {

  private final GlobalApplicationState globalApplicationState;
  private final EventBus eventBus;
  private int keyCounter = 0;

  private final ReactComponent reactComponent;

  private String entityId;
  private ReactRefObject<EntityUploadHandle> ref;

  @Inject
  public UploadDialogWidgetV2(
    GlobalApplicationState globalApplicationState,
    EventBus eventBus
  ) {
    this.globalApplicationState = globalApplicationState;
    this.eventBus = eventBus;
    this.reactComponent = new ReactComponent();
  }

  public void configure(String entityId) {
    this.entityId = entityId;
    this.ref = React.createRef();
    renderComponent(false);
  }

  private void renderComponent(boolean open) {
    EntityUploadModalProps props = EntityUploadModalProps.create(
      entityId,
      open,
      this::onClose,
      this.ref,
      this::onUploadReady
    );

    props.key = String.valueOf(keyCounter);

    reactComponent.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.EntityUploadModal,
        props
      )
    );
  }

  public void show() {
    renderComponent(true);
  }

  private void handleDrop(FileList fileList) {
    if (this.ref != null && this.ref.current != null) {
      this.ref.current.handleUploads(fileList);
      // Show the uploader
      this.show();
    } else {
      DomGlobal.console.error(
        "EntityUploadHandle ref is null, aborting handleDrop"
      );
    }
  }

  private void onUploadReady() {
    globalApplicationState.setDropZoneHandler(this::handleDrop);
  }

  private void onClose() {
    eventBus.fireEvent(new EntityUpdatedEvent(entityId));
    // Re-render the component with a new key to reset the state so it no longer shows the previous set of uploads
    keyCounter++;
    renderComponent(false);
  }

  public void clearDragAndDropHandlers() {
    globalApplicationState.clearDropZoneHandler();
  }

  @Override
  public Widget asWidget() {
    return reactComponent.asWidget();
  }

  @Override
  public void onUnload() {
    clearDragAndDropHandlers();

    super.onUnload();
  }
}
