package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.place.SearchV2Place;
import org.sagebionetworks.web.client.view.SearchV2View;

public class SearchV2Presenter
  extends AbstractActivity
  implements Presenter<SearchV2Place> {

  private final SearchV2View view;

  @Inject
  public SearchV2Presenter(SearchV2View view, SynapseJSNIUtils jsniUtils) {
    this.view = view;

    jsniUtils.setPageTitle(DisplayConstants.WORKING_COPY);
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
  }

  @Override
  public void setPlace(final SearchV2Place place) {
    view.render();
  }
}
