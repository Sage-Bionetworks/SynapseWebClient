package org.sagebionetworks.web.client.presenter.users;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.place.users.RegisterAccount;
import org.sagebionetworks.web.client.presenter.Presenter;

public class RegisterAccountPresenter
  extends AbstractActivity
  implements Presenter<RegisterAccount> {

  private final OneSageUtils oneSageUtils;

  @Inject
  public RegisterAccountPresenter(OneSageUtils oneSageUtils) {
    this.oneSageUtils = oneSageUtils;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {}

  @Override
  public void setPlace(RegisterAccount place) {
    String emailInvitationToken = place.getParam(
      RegisterAccount.MEMBERSHIP_INVTN_QUERY_PARAM
    );
    String email = place.getParam(RegisterAccount.EMAIL_QUERY_PARAM);
    StringBuilder targetUrl = new StringBuilder();
    targetUrl.append(oneSageUtils.getOneSageURL("/register1"));

    if (emailInvitationToken != null) {
      targetUrl.append("&signedToken=" + emailInvitationToken);
    }
    if (email != null) {
      targetUrl.append("&email=" + email);
    }
    Window.Location.replace(targetUrl.toString());
  }
}
