package org.sagebionetworks.web.client.view;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;
import org.sagebionetworks.web.client.SynapseView;

public interface TwoFactorAuthView extends IsWidget, SynapseView {
  void setPresenter(Presenter twoFactorAuthPresenter);

  void showTwoFactorEnrollmentForm();

  void showGenerateRecoveryCodes(boolean showWarning);

  void setSynAlert(IsWidget w);

  public interface Presenter {
    void onTwoFactorEnrollmentComplete();
    void onGenerateRecoveryCodesComplete();
  }
}
