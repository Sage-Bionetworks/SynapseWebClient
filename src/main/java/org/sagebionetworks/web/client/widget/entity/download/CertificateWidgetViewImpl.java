package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.sagebionetworks.web.client.widget.FullWidthAlert;

public class CertificateWidgetViewImpl
  implements CertificateWidgetView, IsWidget {

  Widget widget;

  @UiField
  FullWidthAlert alert;

  public static final String PASSED_QUIZ_ON =
    "You passed the Synapse Certification Quiz on ";

  public interface Binder extends UiBinder<Widget, CertificateWidgetViewImpl> {}

  private final Binder uiBinder = GWT.create(Binder.class);

  @Inject
  public CertificateWidgetViewImpl() {
    widget = uiBinder.createAndBindUi(this);
  }

  @Override
  public void configure(String dateCertified) {
    alert.setMessage(PASSED_QUIZ_ON + dateCertified);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }
}
