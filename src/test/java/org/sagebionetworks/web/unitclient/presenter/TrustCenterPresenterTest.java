package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.place.TrustCenterPlace;
import org.sagebionetworks.web.client.presenter.TrustCenterPresenter;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.RequestBuilderMockStubber;

public class TrustCenterPresenterTest {

  @Mock
  SynapseStandaloneWikiView mockView;

  @Mock
  RequestBuilderWrapper mockRequestBuilder;

  @Mock
  Response mockResponse;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  TrustCenterPlace mockPlace;

  TrustCenterPresenter presenter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    presenter =
      new TrustCenterPresenter(mockView, mockRequestBuilder, mockSynAlert);
  }

  @Test
  public void testValidDocumentKey() throws RequestException {
    when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
    String markdown = "Privacy Policy **markdown**";
    when(mockResponse.getText()).thenReturn(markdown);
    RequestBuilderMockStubber
      .callOnResponseReceived(null, mockResponse)
      .when(mockRequestBuilder)
      .sendRequest(any(), any());
    when(mockPlace.getDocumentKey())
      .thenReturn(TrustCenterPlace.PRIVACY_POLICY_KEY);

    presenter.setPlace(mockPlace);

    verify(mockSynAlert, atLeastOnce()).clear();
    verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), anyString());
    verify(mockRequestBuilder)
      .setHeader(
        WebConstants.CONTENT_TYPE,
        WebConstants.TEXT_PLAIN_CHARSET_UTF8
      );
    verify(mockSynAlert, never()).handleException(any(Throwable.class));
    verify(mockView).configure(markdown);
  }

  @Test
  public void testInvalidDocumentKey() throws RequestException {
    when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
    when(mockPlace.getDocumentKey()).thenReturn("Invalid Key");

    presenter.setPlace(mockPlace);

    verify(mockSynAlert, atLeastOnce()).clear();
    verify(mockSynAlert).showError(any());
    verify(mockRequestBuilder, never())
      .configure(eq(RequestBuilder.GET), any());
  }

  @Test
  public void testUnableToGetMarkdown() throws RequestException {
    when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);
    when(mockPlace.getDocumentKey())
      .thenReturn(TrustCenterPlace.PRIVACY_POLICY_KEY);
    RequestBuilderMockStubber
      .callOnError(null, new Exception())
      .when(mockRequestBuilder)
      .sendRequest(any(), any());

    presenter.setPlace(mockPlace);

    verify(mockRequestBuilder).configure(eq(RequestBuilder.GET), any());
    verify(mockSynAlert, atLeastOnce()).clear();
    verify(mockSynAlert).handleException(any());
  }
}
