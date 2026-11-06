package org.sagebionetworks.web.client;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceChangeRequestEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler.Historian;
import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.user.client.Window.ClosingHandler;
import com.google.web.bindery.event.shared.EventBus;

/**
 * A {@link PlaceController} that keeps its current place in step with the URL, so that browser Back/Forward shows the
 * page for the URL it goes to.
 * <ul>
 * <li>A page can push or replace its own URL to show another of its views without a place change (an entity page's
 * tabs, a wiki subpage, see {@link GlobalApplicationState#pushCurrentPlace(Place)}). The stock PlaceController would
 * still report the page's first place, and {@link PlaceController#goTo(Place)} does nothing when asked for the
 * current place, so Back to that first URL would change the URL but not the page.</li>
 * <li>When Back/Forward asks to leave a page with unsaved changes and the user cancels, the browser has already moved
 * to the other URL. This puts the URL of the page that is still shown back.</li>
 * </ul>
 */
public class SynapsePlaceController extends PlaceController {

  private final EventBus eventBus;
  private final RecordingDelegate delegate;
  private final PlaceHistoryMapper historyMapper;
  private final Historian historian;
  private Place where = Place.NOWHERE;

  public SynapsePlaceController(
    EventBus eventBus,
    Delegate delegate,
    PlaceHistoryMapper historyMapper,
    Historian historian
  ) {
    this(eventBus, new RecordingDelegate(delegate), historyMapper, historian);
  }

  private SynapsePlaceController(
    EventBus eventBus,
    RecordingDelegate delegate,
    PlaceHistoryMapper historyMapper,
    Historian historian
  ) {
    super(eventBus, delegate);
    this.eventBus = eventBus;
    this.delegate = delegate;
    this.historyMapper = historyMapper;
    this.historian = historian;
    // Also follows place changes that are not made by goTo (see showAgain). Registered before any other handler, so
    // handlers of a place change see the new place.
    eventBus.addHandler(
      PlaceChangeEvent.TYPE,
      event -> where = event.getNewPlace()
    );
  }

  @Override
  public Place getWhere() {
    return where;
  }

  /**
   * Records the place for a URL that the current page pushed or replaced to show another of its views.
   */
  public void setWhere(Place place) {
    where = place;
  }

  @Override
  public void goTo(Place newPlace) {
    // confirm() sets this when the current page asks the user; reset it so that it tells about this goTo only
    delegate.isDeclined = false;
    super.goTo(newPlace);
    if (delegate.isDeclined) {
      // The user chose to stay. If Back/Forward asked, the URL is already the other page's, so put this page's back.
      Place urlPlace = historyMapper.getPlace(historian.getToken());
      String token = historyMapper.getToken(where);
      if (!where.equals(urlPlace) && token != null) {
        historian.newItem(token, false);
      }
    }
  }

  /**
   * Shows the current place again, e.g. to reload it, which {@link #goTo(Place)} does not do. As for a place change,
   * the current page may first ask the user to confirm, e.g. to drop unsaved changes.
   */
  public void showAgain(Place place) {
    PlaceChangeRequestEvent request = new PlaceChangeRequestEvent(place);
    eventBus.fireEvent(request);
    String warning = request.getWarning();
    if (warning == null || delegate.confirm(warning)) {
      eventBus.fireEvent(new PlaceChangeEvent(place));
    }
  }

  /** Records whether the user declined to leave the current place. */
  private static class RecordingDelegate implements Delegate {

    private final Delegate delegate;
    private boolean isDeclined;

    RecordingDelegate(Delegate delegate) {
      this.delegate = delegate;
    }

    @Override
    public HandlerRegistration addWindowClosingHandler(ClosingHandler handler) {
      return delegate.addWindowClosingHandler(handler);
    }

    @Override
    public boolean confirm(String message) {
      boolean confirmed = delegate.confirm(message);
      isDeclined = !confirmed;
      return confirmed;
    }
  }
}
