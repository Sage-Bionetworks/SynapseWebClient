package org.sagebionetworks.web.client.presenter.users;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.Presenter;

public class PasswordResetPresenter
  extends AbstractActivity
  implements Presenter<PasswordReset> {

  private static final String ONE_SAGE_RESET_PASSWORD_URL =
    "https://accounts.sagebionetworks.synapse.org/resetPassword";

  @Inject
  public PasswordResetPresenter() {}

  @Override
  public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
    Window.Location.replace(ONE_SAGE_RESET_PASSWORD_URL);
  }

  @Override
  public void setPlace(PasswordReset place) {}
}
