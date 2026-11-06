package org.sagebionetworks.web.unitclient;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceChangeRequestEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationStateImpl;
import org.sagebionetworks.web.client.GlobalApplicationStateView;
import org.sagebionetworks.web.client.OneSageUtils;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.StackConfigServiceAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapsePlaceController;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cache.SessionStorage;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.mvp.AppPlaceHistoryMapper;
import org.sagebionetworks.web.client.place.Home;
import org.sagebionetworks.web.client.place.Profile;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.place.Synapse.ProfileArea;
import org.sagebionetworks.web.client.widget.header.Header;

/**
 * Browser Back/Forward through the real GWT place framework ({@link PlaceHistoryHandler}, {@link SynapsePlaceController})
 * and {@link GlobalApplicationStateImpl}, with a fake browser history in place of
 * {@link org.sagebionetworks.web.client.Html5Historian}.
 */
public class BackForwardNavigationTest {

  static final Synapse WIKI = new Synapse("syn123/wiki/1");
  static final Synapse FILES = new Synapse("syn123/files/");
  static final Synapse TABLES = new Synapse("syn123/tables/");
  static final Synapse DISCUSSION = new Synapse("syn123/discussion/");
  static final Synapse OTHER_ENTITY = new Synapse("syn456");

  SimpleEventBus eventBus;
  FakeBrowserHistory history;
  PlaceController.Delegate mockDelegate;
  SynapsePlaceController placeController;
  GlobalApplicationStateImpl globalApplicationState;
  /** Places shown by the app (each PlaceChangeEvent starts the place's page) */
  List<Place> shownPlaces;
  /** Warning that the page shown gives when asked to stop, e.g. because of unsaved changes */
  String mayStopWarning;

  @Before
  public void before() {
    eventBus = new SimpleEventBus();
    history = new FakeBrowserHistory();
    mockDelegate = mock(PlaceController.Delegate.class);
    AppPlaceHistoryMapper historyMapper = new FakeHistoryMapper();
    placeController =
      new SynapsePlaceController(
        eventBus,
        mockDelegate,
        historyMapper,
        history
      );

    GWTWrapper mockGWT = mock(GWTWrapper.class);
    when(mockGWT.getCurrentHistoryToken())
      .thenAnswer(invocation -> history.getToken());
    doAnswer(invocation -> {
        history.newItem(invocation.getArgument(0), false);
        return null;
      })
      .when(mockGWT)
      .newItem(anyString(), anyBoolean());
    doAnswer(invocation -> {
        history.replaceItem(invocation.getArgument(0));
        return null;
      })
      .when(mockGWT)
      .replaceItem(anyString(), anyBoolean());
    PortalGinInjector mockGinInjector = mock(PortalGinInjector.class);
    when(mockGinInjector.getHeader()).thenReturn(mock(Header.class));
    globalApplicationState =
      new GlobalApplicationStateImpl(
        mock(GlobalApplicationStateView.class),
        mock(CookieProvider.class),
        eventBus,
        mock(StackConfigServiceAsync.class),
        mock(SynapseJSNIUtils.class),
        mock(ClientCache.class),
        mockGWT,
        mock(DateTimeUtils.class),
        mock(SynapseJavascriptClient.class),
        mock(SynapseProperties.class),
        mock(SessionStorage.class),
        mockGinInjector,
        mock(OneSageUtils.class)
      );
    globalApplicationState.setPlaceController(placeController);
    globalApplicationState.setAppPlaceHistoryMapper(historyMapper);

    // as the ActivityManager does
    shownPlaces = new ArrayList<>();
    eventBus.addHandler(
      PlaceChangeEvent.TYPE,
      event -> shownPlaces.add(event.getNewPlace())
    );
    eventBus.addHandler(
      PlaceChangeRequestEvent.TYPE,
      event -> event.setWarning(mayStopWarning)
    );

    PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(
      historyMapper,
      history
    );
    historyHandler.register(placeController, eventBus, new Home("0"));
  }

  /** Loads the app at this URL */
  void load(String token) {
    history.load(token);
    // as PlaceHistoryHandler.handleCurrentHistory does
    placeController.goTo(new FakeHistoryMapper().getPlace(token));
    shownPlaces.clear();
  }

  @Test
  public void testBackForwardAfterPushCurrentPlace() {
    load("Synapse:syn123/wiki/1");
    // Click the Files tab, which only pushes its URL
    globalApplicationState.pushCurrentPlace(FILES);
    assertEquals(
      Arrays.asList("Synapse:syn123/wiki/1", "Synapse:syn123/files/"),
      history.entries
    );

    history.back();
    assertEquals("Synapse:syn123/wiki/1", history.getToken());
    history.forward();
    assertEquals("Synapse:syn123/files/", history.getToken());

    assertEquals(Arrays.asList(WIKI, FILES), shownPlaces);
  }

  @Test
  public void testBackForwardAfterSeveralPushes() {
    load("Synapse:syn123/wiki/1");
    globalApplicationState.pushCurrentPlace(FILES);
    globalApplicationState.pushCurrentPlace(TABLES);
    globalApplicationState.pushCurrentPlace(DISCUSSION);

    history.back();
    history.back();
    history.back();
    assertEquals(Arrays.asList(TABLES, FILES, WIKI), shownPlaces);
    shownPlaces.clear();

    history.forward();
    history.forward();
    history.forward();
    assertEquals(Arrays.asList(FILES, TABLES, DISCUSSION), shownPlaces);
    assertEquals(4, history.entries.size());
  }

  @Test
  public void testBackAfterReplaceCurrentPlace() {
    load("Synapse:syn456");
    globalApplicationState.getPlaceChanger().goTo(WIKI);
    // The entity page shows its default tab, which replaces its URL
    globalApplicationState.replaceCurrentPlace(FILES);
    assertEquals(
      Arrays.asList("Synapse:syn456", "Synapse:syn123/files/"),
      history.entries
    );
    shownPlaces.clear();

    history.back();
    history.forward();

    assertEquals(Arrays.asList(OTHER_ENTITY, FILES), shownPlaces);
  }

  @Test
  public void testBackAcrossPlaceChange() {
    load("Synapse:syn123/wiki/1");
    globalApplicationState.pushCurrentPlace(FILES);
    globalApplicationState.getPlaceChanger().goTo(OTHER_ENTITY);
    assertEquals(Arrays.asList(OTHER_ENTITY), shownPlaces);
    shownPlaces.clear();

    history.back();
    history.back();
    history.forward();
    history.forward();

    assertEquals(Arrays.asList(FILES, WIKI, FILES, OTHER_ENTITY), shownPlaces);
  }

  @Test
  public void testBackForwardBetweenProfileTabs() {
    load("Profile:123/profile");
    // ProfilePresenter changes its place's area, then pushes it
    Profile place = (Profile) placeController.getWhere();
    place.setArea(ProfileArea.PROJECTS, null, null);
    globalApplicationState.pushCurrentPlace(place);
    assertEquals("Profile:123/projects", history.getToken());

    history.back();
    history.forward();

    assertEquals(2, shownPlaces.size());
    assertEquals("123/profile", ((Profile) shownPlaces.get(0)).toToken());
    assertEquals("123/projects", ((Profile) shownPlaces.get(1)).toToken());
  }

  @Test
  public void testGoToCurrentPlaceShowsItAgain() {
    load("Synapse:syn123/wiki/1");
    globalApplicationState.pushCurrentPlace(FILES);

    // e.g. a link to the tab that is shown
    globalApplicationState.getPlaceChanger().goTo(FILES);

    assertEquals(Arrays.asList(FILES), shownPlaces);
    assertEquals(
      Arrays.asList("Synapse:syn123/wiki/1", "Synapse:syn123/files/"),
      history.entries
    );
  }

  @Test
  public void testBackCancelledByUnsavedChanges() {
    load("Synapse:syn123/wiki/1");
    globalApplicationState.getPlaceChanger().goTo(OTHER_ENTITY);
    mayStopWarning = "unsaved changes";
    when(mockDelegate.confirm(mayStopWarning)).thenReturn(false);
    shownPlaces.clear();

    history.back();

    assertEquals(Arrays.asList(), shownPlaces);
    assertEquals(OTHER_ENTITY, placeController.getWhere());
    // the URL is the page's again, and Back still goes to the previous page
    assertEquals("Synapse:syn456", history.getToken());
    assertEquals(
      Arrays.asList("Synapse:syn123/wiki/1", "Synapse:syn456"),
      history.entries
    );

    when(mockDelegate.confirm(mayStopWarning)).thenReturn(true);
    history.back();

    assertEquals(Arrays.asList(WIKI), shownPlaces);
    assertEquals("Synapse:syn123/wiki/1", history.getToken());
  }

  @Test
  public void testGoToCancelledByUnsavedChanges() {
    load("Synapse:syn123/wiki/1");
    mayStopWarning = "unsaved changes";
    when(mockDelegate.confirm(mayStopWarning)).thenReturn(false);

    globalApplicationState.getPlaceChanger().goTo(OTHER_ENTITY);

    assertEquals(Arrays.asList(), shownPlaces);
    assertEquals(Arrays.asList("Synapse:syn123/wiki/1"), history.entries);
  }

  @Test
  public void testBackWithoutWarningDoesNotAsk() {
    load("Synapse:syn123/wiki/1");
    globalApplicationState.getPlaceChanger().goTo(OTHER_ENTITY);

    history.back();

    verify(mockDelegate, never()).confirm(anyString());
    assertEquals(Arrays.asList(OTHER_ENTITY, WIKI), shownPlaces);
  }

  static final String TABLES_URL =
    "https://www.synapse.org/Synapse:syn123/tables/";

  @Test
  public void testBackForwardBetweenTableQueriesShowsPageAgain() {
    load("Synapse:syn123/tables/");

    // Back from a query to the table's default query, then Forward to the query again
    globalApplicationState.onHashChange(TABLES_URL + "#query/abc", TABLES_URL);
    globalApplicationState.onHashChange(TABLES_URL, TABLES_URL + "#query/abc");
    // Back from one query to another
    globalApplicationState.onHashChange(
      TABLES_URL + "#query/def",
      TABLES_URL + "#query/abc"
    );

    assertEquals(Arrays.asList(TABLES, TABLES, TABLES), shownPlaces);
    assertEquals(Arrays.asList("Synapse:syn123/tables/"), history.entries);
  }

  @Test
  public void testOtherFragmentChangesDoNotShowPageAgain() {
    load("Synapse:syn123/tables/");

    // e.g. a link to a heading in a wiki
    globalApplicationState.onHashChange(TABLES_URL, TABLES_URL + "#heading");
    // another page, which the place change shows
    globalApplicationState.onHashChange(
      TABLES_URL + "#query/abc",
      "https://www.synapse.org/Synapse:syn456/tables/#query/abc"
    );

    assertEquals(Arrays.asList(), shownPlaces);
  }

  @Test
  public void testTableQueryBackWhileEditingKeepsPage() {
    load("Synapse:syn123/tables/");
    globalApplicationState.setIsEditing(true);

    globalApplicationState.onHashChange(TABLES_URL + "#query/abc", TABLES_URL);

    assertEquals(Arrays.asList(), shownPlaces);
  }

  /** Maps the places these tests use, as the generated AppPlaceHistoryMapper does */
  static class FakeHistoryMapper implements AppPlaceHistoryMapper {

    @Override
    public Place getPlace(String token) {
      String[] prefixAndToken = token.split(":", 2);
      if (prefixAndToken.length < 2) {
        return null;
      }
      switch (prefixAndToken[0]) {
        case "Synapse":
          return new Synapse.Tokenizer().getPlace(prefixAndToken[1]);
        case "Profile":
          return new Profile.Tokenizer().getPlace(prefixAndToken[1]);
        case "Home":
          return new Home.Tokenizer().getPlace(prefixAndToken[1]);
        default:
          return null;
      }
    }

    @Override
    public String getToken(Place place) {
      if (place instanceof Synapse) {
        return "Synapse:" + new Synapse.Tokenizer().getToken((Synapse) place);
      }
      if (place instanceof Profile) {
        return "Profile:" + new Profile.Tokenizer().getToken((Profile) place);
      }
      if (place instanceof Home) {
        return "Home:" + new Home.Tokenizer().getToken((Home) place);
      }
      return null;
    }
  }

  /**
   * A browser's session history: pushing drops the entries after the current one, and Back/Forward move through the
   * entries and fire popstate, as {@link org.sagebionetworks.web.client.Html5Historian} does.
   */
  static class FakeBrowserHistory
    implements PlaceHistoryHandler.Historian, HasValueChangeHandlers<String> {

    final List<String> entries = new ArrayList<>();
    private final SimpleEventBus handlers = new SimpleEventBus();
    private int index = -1;

    void load(String token) {
      entries.clear();
      entries.add(token);
      index = 0;
    }

    void back() {
      index--;
      ValueChangeEvent.fire(this, getToken());
    }

    void forward() {
      index++;
      ValueChangeEvent.fire(this, getToken());
    }

    void replaceItem(String token) {
      entries.set(index, token);
    }

    @Override
    public String getToken() {
      return entries.get(index);
    }

    @Override
    public void newItem(String token, boolean issueEvent) {
      if (token.equals(getToken())) {
        return;
      }
      entries.subList(index + 1, entries.size()).clear();
      entries.add(token);
      index++;
    }

    @Override
    public HandlerRegistration addValueChangeHandler(
      ValueChangeHandler<String> handler
    ) {
      return handlers.addHandler(ValueChangeEvent.getType(), handler);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
      handlers.fireEvent(event);
    }
  }
}
