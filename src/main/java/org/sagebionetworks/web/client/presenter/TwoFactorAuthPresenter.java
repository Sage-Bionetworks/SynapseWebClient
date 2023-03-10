package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.TwoFactorAuthPlace;
import org.sagebionetworks.web.client.view.TwoFactorAuthView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class TwoFactorAuthPresenter
  extends AbstractActivity
  implements TwoFactorAuthView.Presenter, Presenter<TwoFactorAuthPlace> {

  private final TwoFactorAuthView view;
  private final GlobalApplicationState globalApplicationState;

  @Inject
  public TwoFactorAuthPresenter(
    TwoFactorAuthView view,
    GlobalApplicationState globalApplicationState,
    SynapseAlert synAlert
  ) {
    this.view = view;
    this.globalApplicationState = globalApplicationState;
    view.setSynAlert(synAlert);
    view.setPresenter(this);
  }

  public void configureView(final TwoFactorAuthPlace place) {
    if (place.toToken().equals(TwoFactorAuthPlace.BEGIN_ENROLLMENT)) {
      view.showTwoFactorEnrollmentForm();
    } else if (
      place.toToken().equals(TwoFactorAuthPlace.CREATE_RECOVERY_CODES)
    ) {
      view.showGenerateRecoveryCodes(false);
    } else if (
      place.toToken().equals(TwoFactorAuthPlace.REPLACE_RECOVERY_CODES)
    ) {
      view.showGenerateRecoveryCodes(true);
    }
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(this.view.asWidget());
  }

  @Override
  public void setPlace(final TwoFactorAuthPlace place) {
    view.setPresenter(this);
    view.clear();
    configureView(place);
  }

  @Override
  public String mayStop() {
    view.clear();
    return null;
  }

  @Override
  public void onTwoFactorEnrollmentComplete() {
    globalApplicationState
      .getPlaceChanger()
      .goTo(new TwoFactorAuthPlace(TwoFactorAuthPlace.CREATE_RECOVERY_CODES));
  }

  @Override
  public void onGenerateRecoveryCodesComplete() {
    globalApplicationState
      .getPlaceChanger()
      .goTo(
        new Profile(Profile.VIEW_PROFILE_TOKEN, Synapse.ProfileArea.SETTINGS)
      );
  }
}
