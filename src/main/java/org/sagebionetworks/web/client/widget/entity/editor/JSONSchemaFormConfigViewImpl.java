package org.sagebionetworks.web.client.widget.entity.editor;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.TextBox;
import org.sagebionetworks.web.client.DisplayUtils;

public class JSONSchemaFormConfigViewImpl implements JSONSchemaFormConfigView {

  public interface DynamicFormViewImplUiBinder
    extends UiBinder<Widget, JSONSchemaFormConfigViewImpl> {}

  @UiField
  TextBox schemaUrlField;

  @UiField
  TextBox uiSchemaUrlField;

  @UiField
  TextBox postUrlField;

  @UiField
  Button entityFinderButton;

  Widget widget;

  @Inject
  public JSONSchemaFormConfigViewImpl(DynamicFormViewImplUiBinder binder) {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public void initView() {
    schemaUrlField.setValue("");
    uiSchemaUrlField.setValue("");
    postUrlField.setValue("");
  }

  @Override
  public void checkParams() throws IllegalArgumentException {}

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showLoading() {}

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void clear() {}

  @Override
  public String getSchemaUrl() {
    return schemaUrlField.getValue();
  }

  @Override
  public void setSchemaUrl(String schemaUrl) {
    schemaUrlField.setValue(schemaUrl);
  }

  @Override
  public String getUiSchemaUrl() {
    return uiSchemaUrlField.getValue();
  }

  @Override
  public void setUiSchemaUrl(String uiSchemaUrl) {
    uiSchemaUrlField.setValue(uiSchemaUrl);
  }

  @Override
  public String getPostUrl() {
    return postUrlField.getValue();
  }

  @Override
  public void setPostUrl(String postUrl) {
    postUrlField.setValue(postUrl);
  }
}
