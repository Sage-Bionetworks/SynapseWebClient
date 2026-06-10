package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.view.DownloadCartPageView;
import org.sagebionetworks.web.client.widget.sharing.EntityAccessControlListModalWidget;

public class DownloadCartPresenter
  extends AbstractActivity
  implements
    DownloadCartPageView.Presenter,
    Presenter<org.sagebionetworks.web.client.place.DownloadCartPlace> {

  private DownloadCartPageView view;
  private EntityAccessControlListModalWidget aclModal;
  private PortalGinInjector ginInjector;

  @Inject
  public DownloadCartPresenter(
    DownloadCartPageView view,
    PortalGinInjector ginInjector
  ) {
    this.view = view;
    view.setPresenter(this);
    this.ginInjector = ginInjector;
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

  private EntityAccessControlListModalWidget getAccessControlListModalWidget() {
    if (aclModal == null) {
      aclModal = ginInjector.getEntityAccessControlListModalWidget();
    }
    return aclModal;
  }

  @Override
  public void onViewSharingSettingsClicked(String benefactorEntityId) {
    getAccessControlListModalWidget().configure(benefactorEntityId, () -> {});
    getAccessControlListModalWidget().setOpen(true);
  }
}
