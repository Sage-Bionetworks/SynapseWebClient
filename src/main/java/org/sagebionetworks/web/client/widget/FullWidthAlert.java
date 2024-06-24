package org.sagebionetworks.web.client.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.constants.AlertType;
import org.sagebionetworks.web.client.jsinterop.AlertButtonConfig;
import org.sagebionetworks.web.client.jsinterop.FullWidthAlertProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class FullWidthAlert implements IsWidget {

  ReactComponent container;
  String title, message, primaryButtonText, secondaryButtonText, alertType, secondaryButtonTooltipText;
  AlertButtonConfig.Callback onPrimaryClick;
  AlertButtonConfig.Callback onSecondaryClick;
  FullWidthAlertProps.Callback onClose = () -> setVisible(false);
  Boolean isGlobal = true;
  boolean secondaryButtonEnabled = true;

  /**
   * This is a full width info Alert component, with an icon, message and link.
   *
   */
  public FullWidthAlert() {
    container = new ReactComponent();
  }

  private void rerender() {
    Double autoCloseAfterDelayInSeconds = null;
    AlertButtonConfig primaryButtonConfig = null;
    if (primaryButtonText != null && onPrimaryClick != null) {
      primaryButtonConfig =
        AlertButtonConfig.create(primaryButtonText, onPrimaryClick);
    }

    AlertButtonConfig secondaryButtonConfig = null;
    if (
      secondaryButtonText != null &&
      (onSecondaryClick != null || !secondaryButtonEnabled)
    ) { // if button is disabled, it's ok if there's no onClick
      secondaryButtonConfig =
        AlertButtonConfig.create(
          secondaryButtonText,
          onSecondaryClick,
          !secondaryButtonEnabled,
          secondaryButtonTooltipText
        );
    }
    FullWidthAlertProps props = FullWidthAlertProps.create(
      title,
      message,
      primaryButtonConfig,
      secondaryButtonConfig,
      onClose,
      autoCloseAfterDelayInSeconds,
      isGlobal,
      alertType
    );
    ReactNode component = React.createElementWithThemeContext(
      SRC.SynapseComponents.FullWidthAlert,
      props
    );
    container.render(component);
  }

  @Override
  public Widget asWidget() {
    return container;
  }

  public void setVisible(boolean visible) {
    container.setVisible(visible);
  }

  public void setAddStyleNames(String styleNames) {
    container.addStyleName(styleNames);
  }

  public boolean isVisible() {
    return container.isVisible();
  }

  public boolean isAttached() {
    return container.isAttached();
  }

  public void setMessageTitle(String title) {
    this.title = title;
    rerender();
  }

  public void setMessage(String message) {
    this.message = message;
    rerender();
  }

  public void setPrimaryCTAHref(String href) {
    setPrimaryCTAHref(href, "_blank");
  }

  public void setPrimaryCTAHrefTargetSelf(String href) {
    setPrimaryCTAHref(href, "_self");
  }

  private void setPrimaryCTAHref(String href, String target) {
    addPrimaryCTAClickHandler(event -> {
      Window.open(href, target, "");
    });
  }

  public void addPrimaryCTAClickHandler(ClickHandler c) {
    this.onPrimaryClick = () -> c.onClick(null);
    rerender();
  }

  public void setSecondaryButtonEnabled(boolean enabled) {
    this.secondaryButtonEnabled = enabled;
    rerender();
  }

  public void setSecondaryButtonTooltipText(String tooltipText) {
    this.secondaryButtonTooltipText = tooltipText;
    rerender();
  }

  public void addSecondaryCTAClickHandler(ClickHandler c) {
    this.onSecondaryClick = () -> c.onClick(null);
    rerender();
  }

  public void setPrimaryCTAText(String text) {
    this.primaryButtonText = text;
    rerender();
  }

  public void setSecondaryCTAText(String text) {
    // SWC-6159: Do not toUpperCase link text
    this.secondaryButtonText = text;
    rerender();
  }

  public void setSecondaryCTAHref(String href) {
    addSecondaryCTAClickHandler(event -> {
      Window.open(href, "_blank", "");
    });
  }

  public void setAlertType(AlertType type) {
    this.alertType = type.name().toLowerCase();
    rerender();
  }

  public void setOnClose(FullWidthAlertProps.Callback onClose) {
    this.onClose = onClose;
    rerender();
  }

  public void setGlobal(boolean isGlobal) {
    this.isGlobal = isGlobal;
  }
}
