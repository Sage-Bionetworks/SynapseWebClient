package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.widget.certificationquiz.CertificationQuiz;
import org.sagebionetworks.web.client.widget.header.Header;

public class CertificationQuizViewImpl
  extends Composite
  implements CertificationQuizView {

  public interface CertificationViewImplUiBinder
    extends UiBinder<Widget, CertificationQuizViewImpl> {}

  @UiField
  SimplePanel quizContainer;

  private Header headerWidget;
  private SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public CertificationQuizViewImpl(
    CertificationViewImplUiBinder binder,
    Header headerWidget,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    initWidget(binder.createAndBindUi(this));
    this.headerWidget = headerWidget;
    this.propsProvider = propsProvider;
    headerWidget.configure();
  }

  @Override
  public void createReactComponentWidget() {
    CertificationQuiz component = new CertificationQuiz(this.propsProvider);
    quizContainer.clear();
    quizContainer.add(component);
  }
}
