package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.SearchManagementPlace;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class SearchManagementPresenter
  extends AbstractActivity
  implements Presenter<SearchManagementPlace> {

  private final ReactComponent view;

  @Inject
  public SearchManagementPresenter(SynapseJSNIUtils jsniUtils) {
    this.view = new ReactComponent();
    this.view.addStyleName("padding-30");

    jsniUtils.setPageTitle("Search Management");
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
    this.view.render(
        React.createElementWithSynapseContext(
          SRC.SynapseComponents.SearchManagementPage
        )
      );
  }

  @Override
  public void setPlace(final SearchManagementPlace place) {
    view.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.SearchManagementPage
      )
    );
  }
}
