package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.place.TwoFactorAuthPlace;

public class TwoFactorAuthPresenter
  extends AbstractActivity
  implements Presenter<TwoFactorAuthPlace> {

  @Inject
  public TwoFactorAuthPresenter() {}

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    Window.Location.replace(OneSageUtils.getAccountSettingsURL());
  }

  @Override
  public void setPlace(final TwoFactorAuthPlace place) {}
}
