package org.sagebionetworks.web.client.widget.entity.restriction.v2;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Anchor;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Paragraph;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.HasAccessProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class RestrictionWidgetViewImpl implements RestrictionWidgetView {

  public interface Binder extends UiBinder<Widget, RestrictionWidgetViewImpl> {}

  @UiField
  Span synAlertContainer;

  @UiField
  Span linkUI;

  @UiField
  Anchor changeLink;

  @UiField
  Anchor folderViewTermsLink;

  @UiField
  Div folderRestrictionUI;

  @UiField
  Paragraph folderRestrictedMessage;

  @UiField
  Paragraph folderUnrestrictedMessage;

  @UiField
  Span modalsContainer;

  @UiField
  Div hasAccessContainerParent;

  @UiField
  ReactComponent hasAccessContainer;

  Presenter presenter;
  // this UI widget
  Widget widget;
  RestrictionWidgetModalsViewImpl modals;
  SynapseReactClientFullContextPropsProvider propsProvider;

  @Inject
  public RestrictionWidgetViewImpl(
    Binder binder,
    RestrictionWidgetModalsViewImpl modals,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.widget = binder.createAndBindUi(this);
    this.modals = modals;
    this.propsProvider = propsProvider;
    modalsContainer.add(modals);

    changeLink.addClickHandler(event -> {
      presenter.changeClicked();
    });
  }

  @Override
  public void setPresenter(Presenter presenter) {
    this.presenter = presenter;
    modals.setPresenter(presenter);
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  public void showVerifyDataSensitiveDialog() {
    modals.resetImposeRestrictionModal();
    modals.lazyConstruct();
    modals.imposeRestrictionModal.show();
  }

  @Override
  public void showInfo(String message) {
    DisplayUtils.showInfo(message);
  }

  @Override
  public void showErrorMessage(String message) {
    DisplayUtils.showErrorMessage(message);
  }

  @Override
  public void showLoading() {}

  @Override
  public void showControlledUseUI() {
    folderRestrictedMessage.setVisible(true);
    folderUnrestrictedMessage.setVisible(false);
  }

  @Override
  public void showChangeLink() {
    linkUI.setVisible(true);
    changeLink.setVisible(true);
  }

  @Override
  public void showNoRestrictionsUI() {
    folderRestrictedMessage.setVisible(false);
    folderUnrestrictedMessage.setVisible(true);
  }

  @Override
  public void showFolderRestrictionsLink(String entityId) {
    folderViewTermsLink.setVisible(true);
    folderViewTermsLink.setHref(
      "/AccessRequirements:TYPE=ENTITY&ID=" + entityId
    );
    folderViewTermsLink.setTarget("_blank");
  }

  @Override
  public void clear() {
    linkUI.setVisible(false);
    changeLink.setVisible(false);
    modals.resetImposeRestrictionModal();
  }

  @Override
  public void setNotSensitiveHumanDataMessageVisible(boolean visible) {
    modals.lazyConstruct();
    modals.notSensitiveHumanDataMessage.setVisible(visible);
  }

  @Override
  public Boolean isNoHumanDataRadioSelected() {
    modals.lazyConstruct();
    return modals.noHumanDataRadio.getValue();
  }

  @Override
  public Boolean isYesHumanDataRadioSelected() {
    modals.lazyConstruct();
    return modals.yesHumanDataRadio.getValue();
  }

  @Override
  public void setImposeRestrictionModalVisible(boolean visible) {
    modals.lazyConstruct();
    if (visible) {
      modals.imposeRestrictionModal.show();
    } else {
      modals.imposeRestrictionModal.hide();
    }
  }

  @Override
  public void showFolderRestrictionUI() {
    folderRestrictionUI.setVisible(true);
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
  }

  @Override
  public void configureCurrentAccessComponent(
    String entityId,
    Long versionNumber
  ) {
    String versionNumberString = versionNumber == null
      ? null
      : versionNumber.toString();
    // SWC-5821: force remount
    hasAccessContainer.removeFromParent();
    hasAccessContainerParent.add(hasAccessContainer);

    HasAccessProps props = HasAccessProps.create(
      entityId,
      versionNumberString,
      null,
      null
    );
    ReactNode component = React.createElementWithSynapseContext(
      SRC.SynapseComponents.HasAccess,
      props,
      propsProvider.getJsInteropContextProps()
    );
    hasAccessContainer.render(component);
  }
}
