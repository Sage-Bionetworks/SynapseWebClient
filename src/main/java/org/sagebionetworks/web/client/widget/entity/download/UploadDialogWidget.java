package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.events.EntityUpdatedEvent;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.client.widget.sharing.EntityAccessControlListModalWidget;

public class UploadDialogWidget
  implements UploadDialogWidgetView.Presenter, SynapseWidgetPresenter {

  private UploadDialogWidgetView view;
  private Uploader uploader;
  private final EventBus eventBus;
  private final EntityAccessControlListModalWidget entityAclEditor;

  @Inject
  public UploadDialogWidget(
    UploadDialogWidgetView view,
    Uploader uploader,
    EventBus eventBus,
    EntityAccessControlListModalWidget entityAccessControlListModalWidget
  ) {
    this.view = view;
    this.uploader = uploader;
    this.eventBus = eventBus;
    this.entityAclEditor = entityAccessControlListModalWidget;
    view.setPresenter(this);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void configure(
    String title,
    Entity entity,
    String parentEntityId,
    final CallbackP<String> fileHandleIdCallback,
    boolean isEntity
  ) {
    Widget body = uploader.configure(
      entity,
      parentEntityId,
      fileHandleIdCallback,
      isEntity
    );
    view.configureDialog(title, body);

    // add handlers for closing the window
    uploader.setSuccessHandler(benefactorId -> {
      view.hideDialog();
      if (benefactorId != null) {
        entityAclEditor.configure(
          benefactorId,
          () -> eventBus.fireEvent(new EntityUpdatedEvent(benefactorId)),
          true
        );
        entityAclEditor.setOpen(true);
      }
    });

    uploader.setCancelHandler(() -> {
      view.hideDialog();
    });
  }

  public void disableMultipleFileUploads() {
    uploader.disableMultipleFileUploads();
  }

  public void setUploaderLinkNameVisible(boolean visible) {
    uploader.setUploaderLinkNameVisible(visible);
  }

  public void show() {
    view.showDialog();
  }
}
