package org.sagebionetworks.web.client.presenter.users;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.users.PasswordReset;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.shared.WebConstants;

public class PasswordResetPresenter
  extends AbstractActivity
  implements Presenter<PasswordReset> {

  private static final String ONE_SAGE_RESET_PASSWORD_URL =
    WebConstants.ONESAGE_PRODUCTION_URL +
    "/resetPassword?" +
    WebConstants.ONESAGE_SYNAPSE_APPID_QUERY_PARAM;

  @Inject
  public PasswordResetPresenter() {}

  @Override
  public void start(AcceptsOneWidget acceptsOneWidget, EventBus eventBus) {
    Window.Location.replace(ONE_SAGE_RESET_PASSWORD_URL);
  }

  @Override
  public void setPlace(PasswordReset place) {}
}
