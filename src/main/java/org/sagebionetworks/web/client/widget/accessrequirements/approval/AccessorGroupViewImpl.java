package org.sagebionetworks.web.client.widget.accessrequirements.approval;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.List;
import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.Modal;
import org.gwtbootstrap3.client.ui.ModalBody;
import org.gwtbootstrap3.client.ui.ModalFooter;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.repo.model.dataaccess.AccessApprovalNotification;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.widget.TextBoxWithCopyToClipboardWidget;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class AccessorGroupViewImpl implements AccessorGroupView {

  @UiField
  Div synAlertContainer;

  @UiField
  Div accessorsContainer;

  @UiField
  Div submittedByContainer;

  @UiField
  AnchorListItem showAccessRequirementItem;

  @UiField
  AnchorListItem showNotificationsItem;

  @UiField
  Div emailsContainer;

  @UiField
  Button revokeAccessButton;

  @UiField
  Label expiresOnField;

  Modal dialog;
  Div dialogBodyDiv = new Div();

  Presenter presenter;
  PortalGinInjector ginInjector;
  DateTimeUtils dateTimeUtils;

  public interface Binder extends UiBinder<Widget, AccessorGroupViewImpl> {}

  Widget w;

  @Inject
  public AccessorGroupViewImpl(
    Binder binder,
    PortalGinInjector ginInjector,
    DateTimeUtils dateTimeUtils
  ) {
    this.w = binder.createAndBindUi(this);
    this.ginInjector = ginInjector;
    this.dateTimeUtils = dateTimeUtils;
    showAccessRequirementItem.addClickHandler(event -> {
      presenter.onShowAccessRequirement();
    });
    revokeAccessButton.addClickHandler(event -> {
      presenter.onRevoke();
    });
    showNotificationsItem.addClickHandler(event -> {
      presenter.onShowNotifications();
    });
  }

  @Override
  public void addStyleNames(String styleNames) {
    w.addStyleName(styleNames);
  }

  @Override
  public Widget asWidget() {
    return w;
  }

  @Override
  public void setVisible(boolean visible) {
    w.setVisible(visible);
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }

  @Override
  public void addAccessor(IsWidget w) {
    accessorsContainer.add(w);
  }

  @Override
  public void clearAccessors() {
    accessorsContainer.clear();
  }

  @Override
  public void setSubmittedBy(IsWidget w) {
    submittedByContainer.clear();
    submittedByContainer.add(w);
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public void showAccessRequirementDialog(IsWidget w) {
    dialogBodyDiv.clear();
    dialogBodyDiv.add(w);

    Modal modal = getModal();
    modal.addStyleName("modal-fullscreen");
    modal.setTitle("Access Requirement");
    modal.show();
  }

  @Override
  public void showNotifications(
    List<AccessApprovalNotification> notifications
  ) {
    dialogBodyDiv.clear();
    // show each notification
    for (AccessApprovalNotification notification : notifications) {
      Div div = new Div();
      div.addStyleName("flexcontainer-row flexcontainer-align-items-center");

      Span s = new Span();
      s.addStyleName("flexcontainer-column");
      s.setMarginRight(10);
      s.setText(
        dateTimeUtils.getDateTimeString(notification.getSentOn()) + " : "
      );
      div.add(s);

      UserBadge badge = ginInjector.getUserBadgeWidget();
      badge.configure(notification.getRecipientId().toString());
      badge.addStyleNames("flexcontainer-column");
      div.add(badge);

      s = new Span();
      s.addStyleName("flexcontainer-column");
      s.setMarginLeft(10);
      s.setText(notification.getNotificationType().toString());
      div.add(s);

      dialogBodyDiv.add(div);
    }
    Modal modal = getModal();
    modal.removeStyleName("modal-fullscreen");
    modal.setTitle("Notifications");
    modal.show();
  }

  @Override
  public void setExpiresOn(String expiresOnString) {
    expiresOnField.setText(expiresOnString);
  }

  @Override
  public void clearEmails() {
    emailsContainer.clear();
  }

  @Override
  public void addEmail(String username) {
    TextBoxWithCopyToClipboardWidget emailTextBox = new TextBoxWithCopyToClipboardWidget();
    emailTextBox.setText(username + "@synapse.org");
    emailTextBox.setAddStyleNames("displayBlock");
    emailsContainer.add(emailTextBox);
  }

  private Modal getModal() {
    if (dialog == null) {
      dialog = new Modal();
      Button dialogCloseButton = new Button("Close");
      dialogCloseButton.addClickHandler(event -> {
        dialog.hide();
      });
      ModalBody body = new ModalBody();
      body.add(dialogBodyDiv);
      dialog.add(body);

      ModalFooter footer = new ModalFooter();
      footer.add(dialogCloseButton);
      dialog.add(footer);
    }
    return dialog;
  }
}
