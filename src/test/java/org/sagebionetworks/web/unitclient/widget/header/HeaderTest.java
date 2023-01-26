package org.sagebionetworks.web.unitclient.widget.header;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieKeys;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.security.AuthenticationControllerImpl;
import org.sagebionetworks.web.client.widget.header.Header;
import org.sagebionetworks.web.client.widget.header.HeaderView;

public class HeaderTest {

  Header header;

  @Mock
  HeaderView mockView;

  @Mock
  SynapseJSNIUtils mockSynapseJSNIUtils;

  AdapterFactory adapterFactory = new AdapterFactoryImpl();

  @Mock
  CookieProvider mockCookies;

  @Mock
  ClientCache mockLocalStorage;

  @Mock
  UserProfile mockUserProfile;

  @Mock
  EventBus mockEventBus;

  @Mock
  EventBinder<Header> mockEventBinder;

  JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();

  @Captor
  ArgumentCaptor<JSONObjectAdapter> jsonObjectAdapterCaptor;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(mockView.getEventBinder()).thenReturn(mockEventBinder);
    // by default, mock that we are on the production website
    when(mockSynapseJSNIUtils.getCurrentHostName())
      .thenReturn(Header.WWW_SYNAPSE_ORG);
    header =
      new Header(
        mockView,
        mockSynapseJSNIUtils,
        mockEventBus,
        mockCookies,
        mockLocalStorage,
        jsonObjectAdapter
      );
  }

  @Test
  public void testConstructor() {
    verify(mockView).setPresenter(header);
    verify(mockView).setStagingAlertVisible(false);
    verify(mockView).setCookieNotificationVisible(true);
  }

  @Test
  public void testAsWidget() {
    header.asWidget();
  }

  @Test
  public void testInitStagingAlert() {
    // case insensitive
    Mockito.reset(mockView);
    when(mockSynapseJSNIUtils.getCurrentHostName())
      .thenReturn("WwW.SynapsE.ORG");
    header.initStagingAlert();
    verify(mockView).setStagingAlertVisible(false);

    // staging
    Mockito.reset(mockView);
    when(mockSynapseJSNIUtils.getCurrentHostName())
      .thenReturn("staging.synapse.org");
    header.initStagingAlert();
    verify(mockView).setStagingAlertVisible(true);

    // local
    Mockito.reset(mockView);
    when(mockSynapseJSNIUtils.getCurrentHostName()).thenReturn("localhost");
    header.initStagingAlert();
    verify(mockView).setStagingAlertVisible(true);
  }

  @Test
  public void testRefresh() {
    header.refresh();

    verify(mockView).refresh();
  }

  @Test
  public void testInitWithAcceptCookies() {
    when(
      mockLocalStorage.contains(AuthenticationControllerImpl.COOKIES_ACCEPTED)
    )
      .thenReturn(true);
    reset(mockView);
    when(mockView.getEventBinder()).thenReturn(mockEventBinder);

    header =
      new Header(
        mockView,
        mockSynapseJSNIUtils,
        mockEventBus,
        mockCookies,
        mockLocalStorage,
        jsonObjectAdapter
      );

    verify(mockView).setCookieNotificationVisible(false);
  }

  @Test
  public void testOnCookieNotificationDismissed() {
    header.onCookieNotificationDismissed();

    verify(mockLocalStorage)
      .put(
        eq(AuthenticationControllerImpl.COOKIES_ACCEPTED),
        eq(Boolean.TRUE.toString()),
        any(Long.class)
      );
  }

  @Test
  public void testInitWithNIHNotificationDismissed() {
    when(
      mockLocalStorage.contains(
        AuthenticationControllerImpl.NIH_NOTIFICATION_DISMISSED
      )
    )
      .thenReturn(true);
    reset(mockView);
    when(mockView.getEventBinder()).thenReturn(mockEventBinder);

    header =
      new Header(
        mockView,
        mockSynapseJSNIUtils,
        mockEventBus,
        mockCookies,
        mockLocalStorage,
        jsonObjectAdapter
      );

    verify(mockView).setNIHAlertVisible(false);
  }

  @Test
  public void testOnNIHNotificationDismissed() {
    header.onNIHNotificationDismissed();

    verify(mockLocalStorage)
      .put(
        eq(AuthenticationControllerImpl.NIH_NOTIFICATION_DISMISSED),
        eq(Boolean.TRUE.toString()),
        any(Long.class)
      );
  }

  @Test
  public void testRefreshNoPortalBanner() {
    String cookieValue = null;
    when(mockCookies.getCookie(CookieKeys.PORTAL_CONFIG))
      .thenReturn(cookieValue);

    header =
      new Header(
        mockView,
        mockSynapseJSNIUtils,
        mockEventBus,
        mockCookies,
        mockLocalStorage,
        jsonObjectAdapter
      );

    // should be hidden
    boolean isVisible = false;
    verify(mockView, times(2)).setPortalAlertVisible(isVisible, null);
  }

  @Test
  public void testRefreshWithPortalBanner() throws JSONObjectAdapterException {
    String cookieValue =
      "{\"isInvokingDownloadTable\":true,\"foregroundColor\":\"rgb(255, 255, 255)\",\"backgroundColor\":\"rgb(77, 84, 145)\",\"callbackUrl\":\"https://staging.adknowledgeportal.synapse.org/#/Explore/Data\",\"logoUrl\":\"https://staging.adknowledgeportal.synapse.org/static/media/amp-footer-logo.0e5d7cab.svg\",\"portalName\":\"  \"}";
    when(mockCookies.getCookie(CookieKeys.PORTAL_CONFIG))
      .thenReturn(cookieValue);

    header =
      new Header(
        mockView,
        mockSynapseJSNIUtils,
        mockEventBus,
        mockCookies,
        mockLocalStorage,
        jsonObjectAdapter
      );

    // should be shown
    boolean isVisible = true;
    verify(mockView)
      .setPortalAlertVisible(eq(isVisible), jsonObjectAdapterCaptor.capture());

    // verify json values
    JSONObjectAdapter json = jsonObjectAdapterCaptor.getValue();
    assertTrue(json.getBoolean("isInvokingDownloadTable"));
    assertEquals(
      "https://staging.adknowledgeportal.synapse.org/static/media/amp-footer-logo.0e5d7cab.svg",
      json.getString("logoUrl")
    );
  }
}
