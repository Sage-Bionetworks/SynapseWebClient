package org.sagebionetworks.web.client.presenter.users;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.Presenter;

public class PasswordResetPresenter
  extends AbstractActivity
  implements Presenter<PasswordReset> {

  private final OneSageUtils oneSageUtils;
  private final GWTWrapper gwt;

  @Inject
  public PasswordResetPresenter(OneSageUtils oneSageUtils, GWTWrapper gwt) {
    this.oneSageUtils = oneSageUtils;
    this.gwt = gwt;
  }

  @Override
  public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
    gwt.replaceCurrentWindowWith(oneSageUtils.getOneSageURL("/resetPassword"));
  }

  @Override
  public void setPlace(PasswordReset place) {}
}
