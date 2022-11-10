package org.sagebionetworks.web.client.widget.subscription;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.constants.ButtonSize;
import org.sagebionetworks.web.client.SynapseView;

public interface SubscribeButtonWidgetView extends IsWidget, SynapseView {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void showUnfollowButton();

  void showFollowButton();

  void showUnfollowIcon();

  void showFollowIcon();

  void addStyleNames(String styleNames);

  void setSynAlert(Widget w);

  void showLoading();

  void hideLoading();

  void setButtonSize(ButtonSize size);

  /**
   * Presenter interface
   */
  public interface Presenter {
    void onSubscribe();

    void onUnsubscribe();
  }
}
