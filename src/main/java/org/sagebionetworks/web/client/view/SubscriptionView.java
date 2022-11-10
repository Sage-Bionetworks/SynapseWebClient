package org.sagebionetworks.web.client.view;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.SynapseView;

public interface SubscriptionView extends IsWidget, SynapseView {
  void setPresenter(Presenter presenter);

  void setSynAlert(Widget w);

  void setTopicWidget(Widget w);

  void selectSubscribedButton();

  void selectUnsubscribedButton();

  public interface Presenter {
    void onSubscribe();

    void onUnsubscribe();
  }
}
