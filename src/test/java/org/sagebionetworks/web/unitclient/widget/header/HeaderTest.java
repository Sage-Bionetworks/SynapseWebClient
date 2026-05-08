package org.sagebionetworks.web.unitclient.widget.header;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.web.bindery.event.shared.binder.EventBinder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.UserProfile;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.org.json.AdapterFactoryImpl;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.cache.ClientCache;
import org.sagebionetworks.web.client.cookie.CookieProvider;
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
        mockLocalStorage
      );
  }

  @Test
  public void testConstructor() {
    verify(mockView).setPresenter(header);
    verify(mockView).setStagingAlertVisible(false);
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
}
