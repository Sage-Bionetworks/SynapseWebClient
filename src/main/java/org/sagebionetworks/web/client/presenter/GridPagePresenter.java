package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.GridPlace;
import org.sagebionetworks.web.client.view.GridPageView;

public class GridPagePresenter
  extends AbstractActivity
  implements Presenter<GridPlace> {

  private final GridPageView view;

  @Inject
  public GridPagePresenter(GridPageView view, SynapseJSNIUtils jsniUtils) {
    this.view = view;

    jsniUtils.setPageTitle(DisplayConstants.WORKING_COPY);
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
  }

  @Override
  public void setPlace(final GridPlace place) {
    view.render();
  }
}
