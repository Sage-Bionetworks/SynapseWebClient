package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.view.CertificationQuizView;

public class QuizPresenter
  extends AbstractActivity
  implements Presenter<org.sagebionetworks.web.client.place.Quiz> {

  private CertificationQuizView SRCview;
  SynapseJavascriptClient jsClient;
  private org.sagebionetworks.web.client.place.Quiz place;

  @Inject
  public QuizPresenter(CertificationQuizView srcView) {
    this.SRCview = srcView;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(SRCview);
    SRCview.createReactComponentWidget();
  }

  @Override
  public void setPlace(org.sagebionetworks.web.client.place.Quiz place) {
    this.place = place;
  }
}
