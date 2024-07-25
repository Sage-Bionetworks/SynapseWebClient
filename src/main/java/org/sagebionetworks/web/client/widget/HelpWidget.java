package org.sagebionetworks.web.client.widget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.gwtbootstrap3.client.ui.constants.Placement;
import org.gwtbootstrap3.client.ui.constants.Pull;
import org.gwtbootstrap3.client.ui.html.Span;
import org.sagebionetworks.web.client.jsinterop.HelpPopoverProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.ReactNode;
import org.sagebionetworks.web.client.jsinterop.SRC;

/**
 * View only widget used to show a help icon (and help text). When clicked, a popover is shown that
 * contains basic help, and a More Info button. When the More Info button is clicked, the browser
 * will open a new tab to the full help documentation (typically to the help.synapse.org site).
 *
 * ## Usage
 *
 * In your ui.xml, add the help widget. ```
 * xmlns:w="urn:import:org.sagebionetworks.web.client.widget"
 * <w:HelpWidget text="Optional help link text" help="This contains concise but basic help." href=
 * "http://link/to/more/help" /> ```
 *
 * That's it! You can set visibility and placement today, and we can easily extend it to have
 * additional options in the future.
 *
 * @author jayhodgson
 *
 */
public class HelpWidget implements IsWidget {

  Span widget = new Span();
  ReactComponent helpPopoverWrapper = new ReactComponent();

  String text;
  String md;
  String link;
  String placement;
  boolean showCloseButton = true;
  String className = "displayInline";

  public HelpWidget() {
    widget.addStyleName("HelpWidget");
  }

  protected void updateContent() {
    helpPopoverWrapper.removeFromParent();
    widget.add(helpPopoverWrapper);
    HelpPopoverProps props = HelpPopoverProps.create(
      md,
      link,
      placement,
      showCloseButton,
      className
    );
    ReactNode component = React.createElement(
      SRC.SynapseComponents.HelpPopover,
      props
    );
    helpPopoverWrapper.render(component);
  }

  public void setText(String text) {
    this.text = text;
    updateContent();
  }

  public void setHelpMarkdown(String md) {
    this.md = md;
    updateContent();
  }

  public void setHref(String fullHelpHref) {
    link = fullHelpHref;
    updateContent();
  }

  @Override
  public Widget asWidget() {
    updateContent();
    return widget;
  }

  public void setVisible(boolean visible) {
    widget.setVisible(visible);
  }

  public void setPlacement(final Placement placement) {
    this.placement = placement.name().toLowerCase();
    updateContent();
  }

  public void setAddStyleNames(String styleNames) {
    className += styleNames;
    updateContent();
  }

  public void setPull(Pull pull) {
    widget.addStyleName(pull.getCssName());
  }
}
