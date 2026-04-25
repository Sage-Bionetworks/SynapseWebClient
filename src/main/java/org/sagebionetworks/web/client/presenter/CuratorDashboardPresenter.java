package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.CuratorDashboardPlace;
import org.sagebionetworks.web.client.place.GridPlace;
import org.sagebionetworks.web.client.view.GridPageView;
import org.sagebionetworks.web.client.widget.ReactComponent;
import org.sagebionetworks.web.client.widget.ReactComponentV2;

public class CuratorDashboardPresenter
  extends AbstractActivity
  implements Presenter<CuratorDashboardPlace> {

  private final ReactComponent view;

  @Inject
  public CuratorDashboardPresenter(SynapseJSNIUtils jsniUtils) {
    this.view = new ReactComponent();

    jsniUtils.setPageTitle("Curator Dashboard");
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
    this.view.render(
        React.createElementWithSynapseContext(
          SRC.SynapseComponents.CuratorDashboard
        )
      );
  }

  @Override
  public void setPlace(final CuratorDashboardPlace place) {
    view.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.CuratorDashboard
      )
    );
  }
}
