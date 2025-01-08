package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.view.DataCatalogPageView;

public class DataCatalogPagePresenter
  extends AbstractActivity
  implements
    Presenter<org.sagebionetworks.web.client.place.DataCatalogPagePlace> {

  private DataCatalogPageView view;

  @Inject
  public DataCatalogPagePresenter(DataCatalogPageView view) {
    this.view = view;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
  }

  @Override
  public void setPlace(
    final org.sagebionetworks.web.client.place.DataCatalogPagePlace place
  ) {
    view.render();
  }
}
