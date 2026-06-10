package org.sagebionetworks.web.client.widget.team;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import javax.inject.Inject;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.TextBox;

public class JoinTeamConfigEditorViewImpl implements JoinTeamConfigEditorView {

  public interface JoinTeamConfigWidgetViewImplUiBinder
    extends UiBinder<Widget, JoinTeamConfigEditorViewImpl> {}

  @UiField
  SimplePanel suggestBoxPanel;

  @UiField
  CheckBox isChallengeCheckbox;

  @UiField
  TextBox memberMessageField;

  @UiField
  TextBox joinButtonField;

  @UiField
  TextBox joinSuccessField;

  @UiField
  TextBox requestOpenInfoField;

  @UiField
  CheckBox isPromptForMessageCheckbox;

  Widget widget;

  private final JoinTeamConfigWidgetViewImplUiBinder binder = GWT.create(
    JoinTeamConfigWidgetViewImplUiBinder.class
  );

  @Inject
  public JoinTeamConfigEditorViewImpl() {
    widget = binder.createAndBindUi(this);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void setIsChallenge(boolean isChallengeSignup) {
    isChallengeCheckbox.setValue(isChallengeSignup);
  }

  @Override
  public void setIsSimpleRequest(boolean isSimpleRequest) {
    isPromptForMessageCheckbox.setValue(!isSimpleRequest);
  }

  @Override
  public void setIsMemberMessage(String isMemberMessage) {
    memberMessageField.setText(isMemberMessage);
  }

  @Override
  public void setSuccessMessage(String successMessage) {
    joinSuccessField.setText(successMessage);
  }

  @Override
  public void setButtonText(String buttonText) {
    joinButtonField.setText(buttonText);
  }

  @Override
  public void setRequestOpenInfotext(String requestOpenInfoText) {
    requestOpenInfoField.setText(requestOpenInfoText);
  }

  @Override
  public boolean getIsChallenge() {
    return isChallengeCheckbox.getValue();
  }

  @Override
  public boolean getIsSimpleRequest() {
    return !isPromptForMessageCheckbox.getValue();
  }

  @Override
  public String getIsMemberMessage() {
    return memberMessageField.getText();
  }

  @Override
  public String getSuccessMessage() {
    return joinSuccessField.getText();
  }

  @Override
  public String getButtonText() {
    return joinButtonField.getText();
  }

  @Override
  public String getRequestOpenInfotext() {
    return requestOpenInfoField.getText();
  }

  @Override
  public void setSuggestWidget(IsWidget teamSuggestBox) {
    suggestBoxPanel.setWidget(teamSuggestBox);
  }
}
