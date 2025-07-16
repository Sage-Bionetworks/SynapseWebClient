package org.sagebionetworks.web.client.view;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
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

  @Inject
  public CertificationQuizViewImpl(
    CertificationViewImplUiBinder binder,
    Header headerWidget
  ) {
    initWidget(binder.createAndBindUi(this));
    this.headerWidget = headerWidget;
    headerWidget.configure();
  }

  @Override
  public void createReactComponentWidget() {
    CertificationQuiz component = new CertificationQuiz();
    quizContainer.clear();
    quizContainer.add(component);
  }
}
