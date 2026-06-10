package org.sagebionetworks.web.client.widget.entity.download;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.Input;
import org.gwtbootstrap3.client.ui.html.Strong;

public class AwsLoginViewImpl implements AwsLoginView {

  public interface Binder extends UiBinder<Widget, AwsLoginViewImpl> {}

  Widget w;

  @UiField
  Strong endpointField;

  @UiField
  Input accessKeyField;

  @UiField
  Input secretKeyField;

  private final Binder uiBinder = GWT.create(Binder.class);

  @Inject
  public AwsLoginViewImpl() {
    w = uiBinder.createAndBindUi(this);
  }

  @Override
  public void setEndpoint(String value) {
    endpointField.setText(value);
  }

  @Override
  public String getAccessKey() {
    return accessKeyField.getValue();
  }

  @Override
  public String getSecretKey() {
    return secretKeyField.getValue();
  }

  @Override
  public void clear() {
    endpointField.setText("");
    accessKeyField.setValue("");
    secretKeyField.setValue("");
  }

  @Override
  public Widget asWidget() {
    return w;
  }

  @Override
  public void setVisible(boolean visible) {
    w.setVisible(visible);
  }
}
