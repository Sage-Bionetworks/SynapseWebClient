package org.sagebionetworks.web.client.presenter.users;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.Presenter;

public class PasswordResetPresenter
  extends AbstractActivity
  implements Presenter<PasswordReset> {

  private final OneSageUtils oneSageUtils;

  @Inject
  public PasswordResetPresenter(OneSageUtils oneSageUtils) {
    this.oneSageUtils = oneSageUtils;
  }

  @Override
  public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
    Window.Location.replace(oneSageUtils.getOneSageURL("/resetPassword"));
  }

  @Override
  public void setPlace(PasswordReset place) {}
}
