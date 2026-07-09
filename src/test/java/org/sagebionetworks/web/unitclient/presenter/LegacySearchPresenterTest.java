package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PlaceChanger;
import org.sagebionetworks.web.client.place.LegacySearchPlace;
import org.sagebionetworks.web.client.place.SearchV2Place;
import org.sagebionetworks.web.client.place.Synapse;
import org.sagebionetworks.web.client.presenter.LegacySearchPresenter;

@RunWith(MockitoJUnitRunner.Silent.class)
public class LegacySearchPresenterTest {

  LegacySearchPresenter presenter;

  @Mock
  GlobalApplicationState mockGlobalApplicationState;

  @Mock
  PlaceChanger mockPlaceChanger;

  @Mock
  GWTWrapper mockGwt;

  @Mock
  AcceptsOneWidget mockPanel;

  @Mock
  EventBus mockEventBus;

  @Before
  public void before() {
    when(mockGlobalApplicationState.getPlaceChanger())
      .thenReturn(mockPlaceChanger);
    // Return the input unchanged for most tests (no URL encoding)
    when(mockGwt.decodeQueryString(anyString()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    // Return the input unchanged so tests can assert on the raw JSON string
    when(mockGwt.encodeQueryString(anyString()))
      .thenAnswer(invocation -> invocation.getArgument(0));
    // Simulate JSONString.toString(): wrap in double quotes (no actual escaping needed for most test cases)
    when(mockGwt.escapeJsonString(anyString()))
      .thenAnswer(invocation -> "\"" + invocation.getArgument(0) + "\"");
    presenter = new LegacySearchPresenter(mockGlobalApplicationState, mockGwt);
  }

  private void startWithToken(String token) {
    presenter.setPlace(new LegacySearchPlace(token));
    presenter.start(mockPanel, mockEventBus);
  }

  @Test
  public void testSynapseIdRedirectsToSynapsePlace() {
    startWithToken("syn1234567890");

    ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
    verify(mockPlaceChanger).goTo(captor.capture());
    // entity id is lowercased before matching
    org.junit.Assert.assertEquals(
      "syn1234567890",
      captor.getValue().getEntityId()
    );
  }

  @Test
  public void testSynapseIdIsCaseInsensitive() {
    startWithToken("SYN999");

    ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
    verify(mockPlaceChanger).goTo(captor.capture());
    org.junit.Assert.assertEquals("syn999", captor.getValue().getEntityId());
  }

  @Test
  public void testSynapseIdWithVersionRedirectsToSynapsePlace() {
    startWithToken("syn123.5");

    ArgumentCaptor<Synapse> captor = ArgumentCaptor.forClass(Synapse.class);
    verify(mockPlaceChanger).goTo(captor.capture());
    org.junit.Assert.assertEquals("syn123", captor.getValue().getEntityId());
    org.junit.Assert.assertEquals(
      Long.valueOf(5),
      captor.getValue().getVersionNumber()
    );
  }

  @Test
  public void testNonSynapseIdRedirectsToSearchV2Place() {
    startWithToken("cancer genomics");

    ArgumentCaptor<SearchV2Place> captor = ArgumentCaptor.forClass(
      SearchV2Place.class
    );
    verify(mockPlaceChanger).goTo(captor.capture());
    verify(mockGwt).encodeQueryString("{\"queryTerm\":[\"cancer genomics\"]}");
  }

  @Test
  public void testUrlEncodedTokenIsDecodedBeforeSearch() {
    String encodedToken = "cancer%20genomics";
    String decodedToken = "cancer genomics";
    when(mockGwt.decodeQueryString(encodedToken)).thenReturn(decodedToken);

    startWithToken(encodedToken);

    // The decoded value should be used in the query JSON, not the raw encoded token
    verify(mockGwt).escapeJsonString(decodedToken);
    verify(mockGwt).encodeQueryString("{\"queryTerm\":[\"cancer genomics\"]}");
  }

  @Test
  public void testNonSynapseIdSearchTokenContainsQueryParameter() {
    String term = "diabetes";
    startWithToken(term);

    ArgumentCaptor<SearchV2Place> captor = ArgumentCaptor.forClass(
      SearchV2Place.class
    );
    verify(mockPlaceChanger).goTo(captor.capture());
    // token should be: default?query={"queryTerm":["diabetes"]}
    // (encodeQueryString is stubbed to return input unchanged)
    String token = new SearchV2Place.Tokenizer().getToken(captor.getValue());
    org.junit.Assert.assertTrue(
      "Token should start with 'default?query='",
      token.startsWith("default?" + SearchV2Place.QUERY + "=")
    );
    org.junit.Assert.assertTrue(
      "Token should contain the search term",
      token.contains("\"" + term + "\"")
    );
    org.junit.Assert.assertTrue(
      "queryTerm value should be a JSON array",
      token.contains("[\"" + term + "\"]")
    );
  }

  @Test
  public void testQueryJsonIsEncoded() {
    startWithToken("test");

    verify(mockGwt).encodeQueryString(anyString());
  }

  @Test
  public void testTokenWithSpecialCharactersIsEscaped() {
    // Simulate what GWTWrapperImpl.escapeJsonString would do for a token with double quotes
    String token = "he said \"hello\"";
    String escapedToken = "\"he said \\\"hello\\\"\"";
    when(mockGwt.escapeJsonString(token)).thenReturn(escapedToken);

    startWithToken(token);

    verify(mockGwt).escapeJsonString(token);
    ArgumentCaptor<SearchV2Place> captor = ArgumentCaptor.forClass(
      SearchV2Place.class
    );
    verify(mockPlaceChanger).goTo(captor.capture());
    String placeToken = new SearchV2Place.Tokenizer()
      .getToken(captor.getValue());
    org.junit.Assert.assertTrue(
      "Token should contain the escaped JSON value",
      placeToken.contains(escapedToken)
    );
  }

  @Test
  public void testEmptyTokenRedirectsToSearchV2Place() {
    startWithToken("");

    verify(mockPlaceChanger).goTo(any(SearchV2Place.class));
    verify(mockPlaceChanger, never()).goTo(any(Synapse.class));
  }

  @Test
  public void testNonSynapseIdDoesNotRedirectToSynapsePlace() {
    startWithToken("not a synapse id");

    verify(mockPlaceChanger, never()).goTo(any(Synapse.class));
  }
}
