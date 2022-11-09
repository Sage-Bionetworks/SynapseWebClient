package org.sagebionetworks.web.client.widget;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import org.gwtbootstrap3.client.ui.html.Div;

public class ClickableDiv extends Div implements HasClickHandlers {

  public ClickableDiv() {}

  @Override
  public HandlerRegistration addClickHandler(ClickHandler handler) {
    return addDomHandler(handler, ClickEvent.getType());
  }
}
