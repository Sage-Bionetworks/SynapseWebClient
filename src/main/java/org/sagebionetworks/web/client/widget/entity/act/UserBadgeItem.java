package org.sagebionetworks.web.client.widget.entity.act;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.ButtonGroup;
import org.gwtbootstrap3.client.ui.CheckBox;
import org.gwtbootstrap3.client.ui.Radio;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.repo.model.dataaccess.AccessType;
import org.sagebionetworks.repo.model.dataaccess.AccessorChange;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.SelectableListItem;
import org.sagebionetworks.web.client.widget.profile.ProfileCertifiedValidatedWidget;
import org.sagebionetworks.web.client.widget.user.BadgeType;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class UserBadgeItem implements IsWidget, SelectableListItem {

  public interface UserBadgeItemUiBinder
    extends UiBinder<Widget, UserBadgeItem> {}

  @UiField
  CheckBox select;

  @UiField
  Div userBadgeContainer;

  @UiField
  Div renewRevokeContainer;

  @UiField
  ButtonGroup renewRevokeButtonGroup;

  Radio renew;
  Radio revoke;

  Widget widget;

  String userId;
  Callback selectionChangedCallback;
  PortalGinInjector portalGinInjector;
  AccessType accessType;
  AccessorChange change;
  UserProfile profile;

  @Inject
  public UserBadgeItem(
    UserBadgeItemUiBinder binder,
    PortalGinInjector portalGinInjector
  ) {
    widget = binder.createAndBindUi(this);
    this.portalGinInjector = portalGinInjector;
    select.addClickHandler(event -> {
      if (selectionChangedCallback != null) {
        selectionChangedCallback.invoke();
      }
    });

    // get unique ID to assign to Radio button name
    String radioButtonGroupName = DOM.createUniqueId();
    renew = new Radio(radioButtonGroupName, "Renew");
    renew.addStyleName("inline-block margin-right-5");
    renew.addClickHandler(event -> {
      showRenew();
    });
    revoke = new Radio(radioButtonGroupName, "Revoke");
    revoke.addStyleName("inline-block");
    revoke.addClickHandler(event -> {
      showRevoke();
    });
    renewRevokeButtonGroup.add(renew);
    renewRevokeButtonGroup.add(revoke);
  }

  private void showRenew() {
    accessType = AccessType.RENEW_ACCESS;
    renew.setValue(true);
    revoke.setValue(false);
    userBadgeContainer.removeStyleName("strikeout-links lightgrey-links");
  }

  private void showRevoke() {
    accessType = AccessType.REVOKE_ACCESS;
    renew.setValue(false);
    revoke.setValue(true);
    userBadgeContainer.addStyleName("strikeout-links lightgrey-links");
  }

  public UserBadgeItem configure(AccessorChange change, UserProfile profile) {
    this.change = change;
    this.profile = profile;
    userBadgeContainer.clear();
    userId = change.getUserId();
    accessType = change.getType();
    boolean isGainAccess = AccessType.GAIN_ACCESS.equals(accessType);
    renewRevokeContainer.setVisible(!isGainAccess);
    if (!isGainAccess) {
      select.setVisible(false);
    }

    switch (accessType) {
      case RENEW_ACCESS:
        showRenew();
        break;
      case REVOKE_ACCESS:
        showRevoke();
        break;
      default:
    }
    Div badgeContainerRow = new Div();
    badgeContainerRow.addStyleName("flexcontainer-row");

    UserBadge userBadge = portalGinInjector.getUserBadgeWidget();
    if (profile != null) {
      userBadge.configure(profile);
    } else {
      userBadge.configure(userId);
    }
    userBadge.setBadgeType(BadgeType.SMALL_CARD);
    userBadge.setCustomClickHandler(
      new ClickHandler() {
        @Override
        public void onClick(ClickEvent event) {
          setSelected(!isSelected());
          if (selectionChangedCallback != null) {
            selectionChangedCallback.invoke();
          }
        }
      }
    );
    userBadge.asWidget().addStyleName("flexcontainer-column margin-top-10");
    badgeContainerRow.add(userBadge.asWidget());

    ProfileCertifiedValidatedWidget w = portalGinInjector.getProfileCertifiedValidatedWidget();
    w.configure(Long.parseLong(userId));
    Div flexColumn = new Div();
    flexColumn.addStyleName("flexcontainer-column");
    flexColumn.setMarginTop(12);
    flexColumn.setMarginLeft(5);
    flexColumn.add(w);
    badgeContainerRow.add(flexColumn);

    userBadgeContainer.add(badgeContainerRow);
    return this;
  }

  public UserBadgeItem configure(AccessorChange change) {
    return configure(change, null);
  }

  public void reconfigure() {
    configure(change, profile);
  }

  public UserBadgeItem setSelectionChangedCallback(Callback callback) {
    selectionChangedCallback = callback;
    return this;
  }

  public boolean isSelected() {
    return select.getValue();
  }

  public void setSelected(boolean selected) {
    if (select.isVisible()) {
      select.setValue(selected, true);
    }
  }

  public boolean isSelectEnabled() {
    return select.isEnabled();
  }

  public void setSelectEnabled(boolean enabled) {
    select.setEnabled(enabled);
  }

  public void setSelectVisible(boolean visible) {
    select.setVisible(visible);
  }

  public String getUserId() {
    return userId;
  }

  public AccessType getAccessType() {
    return accessType;
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  public void setAccessTypeDropdownEnabled(boolean enabled) {
    renew.setEnabled(enabled);
    revoke.setEnabled(enabled);
  }
}
