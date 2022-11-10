package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.Entity;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DownloadCartPageView;
import org.sagebionetworks.web.client.widget.sharing.AccessControlListModalWidget;

public class DownloadCartPresenter
  extends AbstractActivity
  implements
    DownloadCartPageView.Presenter,
    Presenter<org.sagebionetworks.web.client.place.DownloadCartPlace> {

  private DownloadCartPageView view;
  private AccessControlListModalWidget aclModal;
  private PortalGinInjector ginInjector;
  private PopupUtilsView popupUtils;

  @Inject
  public DownloadCartPresenter(
    DownloadCartPageView view,
    PortalGinInjector ginInjector,
    PopupUtilsView popupUtils
  ) {
    this.view = view;
    view.setPresenter(this);
    this.ginInjector = ginInjector;
    this.popupUtils = popupUtils;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
  }

  @Override
  public void setPlace(
    final org.sagebionetworks.web.client.place.DownloadCartPlace place
  ) {
    view.render();
  }

  private AccessControlListModalWidget getAccessControlListModalWidget() {
    if (aclModal == null) {
      aclModal = ginInjector.getAccessControlListModalWidget();
    }
    return aclModal;
  }

  @Override
  public void onViewSharingSettingsClicked(String benefactorEntityId) {
    ginInjector
      .getSynapseJavascriptClient()
      .getEntity(
        benefactorEntityId,
        new AsyncCallback<Entity>() {
          @Override
          public void onSuccess(Entity entity) {
            boolean canChangePermission = false;
            getAccessControlListModalWidget()
              .configure(entity, canChangePermission);
            getAccessControlListModalWidget().showSharing(() -> {});
          }

          @Override
          public void onFailure(Throwable caught) {
            popupUtils.showErrorMessage(caught.getMessage());
          }
        }
      );
  }
}
