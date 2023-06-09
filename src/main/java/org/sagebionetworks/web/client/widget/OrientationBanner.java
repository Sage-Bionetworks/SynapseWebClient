package org.sagebionetworks.web.client.widget;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.jsinterop.AlertButtonConfig;
import org.sagebionetworks.web.client.jsinterop.OrientationBannerProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;

public class OrientationBanner implements IsWidget {

  ReactComponentDiv container;
  String name, title, text, primaryButtonText, secondaryButtonText, secondaryButtonHref;
  AlertButtonConfig.Callback onPrimaryClick;

  /**
   * This is an OrientationBanner component, with an illustration, title, text, and optional link and call to action buttons.
   *
   */
  public OrientationBanner() {
    container = new ReactComponentDiv();
  }

  private void rerender() {
    AlertButtonConfig primaryButtonConfig = null;
    if (primaryButtonText != null && onPrimaryClick != null) {
      primaryButtonConfig =
        AlertButtonConfig.create(primaryButtonText, onPrimaryClick);
    }

    AlertButtonConfig secondaryButtonConfig = null;
    if (secondaryButtonText != null && secondaryButtonHref != null) {
      secondaryButtonConfig =
        AlertButtonConfig.create(secondaryButtonText, secondaryButtonHref);
    }
    OrientationBannerProps props = OrientationBannerProps.create(
      name,
      title,
      text,
      primaryButtonConfig,
      secondaryButtonConfig
    );
    ReactNode component = React.createElementWithThemeContext(
      SRC.SynapseComponents.OrientationBanner,
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

  public boolean isVisible() {
    return container.isVisible();
  }

  public boolean isAttached() {
    return container.isAttached();
  }

  public void setName(String name) {
    this.name = name;
    rerender();
  }

  public void setTitle(String title) {
    this.title = title;
    rerender();
  }

  public void setText(String text) {
    this.text = text;
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

  public void setPrimaryCTAText(String text) {
    this.primaryButtonText = text;
    rerender();
  }

  public void setSecondaryCTAText(String text) {
    this.secondaryButtonText = text;
    rerender();
  }

  public void setSecondaryCTAHref(String href) {
    this.secondaryButtonHref = href;
    rerender();
  }

  public void configure(
    String name,
    String title,
    String text,
    String primaryButtonText,
    ClickHandler primaryButtonClickHandler,
    String secondaryButtonText,
    String secondaryButtonHref
  ) {
    this.name = name;
    this.title = title;
    this.text = text;
    this.primaryButtonText = primaryButtonText;
    this.secondaryButtonText = secondaryButtonText;
    this.secondaryButtonHref = secondaryButtonHref;
    addPrimaryCTAClickHandler(primaryButtonClickHandler);
  }
}
