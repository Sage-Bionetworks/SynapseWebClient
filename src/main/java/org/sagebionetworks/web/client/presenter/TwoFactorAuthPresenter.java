package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.place.TwoFactorAuthPlace;
import org.sagebionetworks.web.shared.WebConstants;

public class TwoFactorAuthPresenter
  extends AbstractActivity
  implements Presenter<TwoFactorAuthPlace> {

  @Inject
  public TwoFactorAuthPresenter() {}

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    Window.Location.replace(WebConstants.ONESAGE_ACCOUNT_SETTINGS_URL);
  }

  @Override
  public void setPlace(final TwoFactorAuthPlace place) {}
}
