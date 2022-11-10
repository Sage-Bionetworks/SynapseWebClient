package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.OAuthClientEditorPlace;
import org.sagebionetworks.web.client.view.OAuthClientEditorView;

public class OAuthClientEditorPresenter
  extends AbstractActivity
  implements Presenter<OAuthClientEditorPlace> {

  private OAuthClientEditorView view;
  private OAuthClientEditorPlace place;

  @Inject
  public OAuthClientEditorPresenter(OAuthClientEditorView view) {
    this.view = view;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view);
    view.createReactComponentWidget();
  }

  @Override
  public void setPlace(OAuthClientEditorPlace place) {
    this.place = place;
  }

  public OAuthClientEditorPlace getPlace() {
    return place;
  }

  @Override
  public String mayStop() {
    return null;
  }
}
