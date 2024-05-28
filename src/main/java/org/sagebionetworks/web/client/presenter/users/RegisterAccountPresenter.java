package org.sagebionetworks.web.client.presenter.users;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.Presenter;
import org.sagebionetworks.web.shared.WebConstants;

public class RegisterAccountPresenter
  extends AbstractActivity
  implements Presenter<RegisterAccount> {

  @Inject
  public RegisterAccountPresenter() {}

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {}

  @Override
  public void setPlace(RegisterAccount place) {
    String token = place.toToken();
    if (token != null && token.contains("@")) {
      // is likely an email address
      Window.Location.replace(
        WebConstants.ONESAGE_PRODUCTION_URL + "/register1?email=" + token
      );
    } else {
      Window.Location.replace(
        WebConstants.ONESAGE_PRODUCTION_URL + "/register1"
      );
    }
  }
}
