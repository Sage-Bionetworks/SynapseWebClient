package org.sagebionetworks.web.client;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;
import com.google.gwt.user.client.Window;

/**
 * An {@link Historian} using HTML5's {@code pushState} and {@code onpopstate}.
 */
public class Html5Historian
  implements
    Historian,
    // allows the use of ValueChangeEvent.fire()
    HasValueChangeHandlers<String> {

  private final SimpleEventBus handlers = new SimpleEventBus();

  public Html5Historian() {
    initEvent();
  }

  @Override
  public HandlerRegistration addValueChangeHandler(
    ValueChangeHandler<String> valueChangeHandler
  ) {
    return this.handlers.addHandler(
        ValueChangeEvent.getType(),
        valueChangeHandler
      );
  }

  @Override
  public String getToken() {
    return Window.Location.getPath().substring(1);
  }

  @Override
  public void newItem(String token, boolean issueEvent) {
    if (getToken().equals(token)) { // not sure if this is needed, but just in case
      return;
    }
    pushState("/" + token);
    if (issueEvent) {
      ValueChangeEvent.fire(this, getToken());
    }
  }

  public void replaceItem(String token, boolean issueEvent) {
    if (getToken().equals(token)) { // not sure if this is needed, but just in case
      return;
    }
    replaceState("/" + token);
    if (issueEvent) {
      ValueChangeEvent.fire(this, getToken());
    }
  }

  @Override
  public void fireEvent(GwtEvent<?> event) {
    this.handlers.fireEvent(event);
  }

  private native void initEvent() /*-{
    var that = this;
    var oldHandler = $wnd.onpopstate;
    $wnd.onpopstate = $entry(function(e) {
      that.@org.sagebionetworks.web.client.Html5Historian::onPopState()();
      if (oldHandler) {
        oldHandler();
      }
    });
  }-*/;

  private void onPopState() {
    ValueChangeEvent.fire(this, getToken());
  }

  private native void pushState(String url) /*-{
    $wnd.history.pushState(null, $doc.title, url);
  }-*/;

  private native void replaceState(String url) /*-{
      $wnd.history.replaceState(null, $doc.title, url);
  }-*/;
}
