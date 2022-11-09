package org.sagebionetworks.web.unitclient.presenter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.DataAccessApprovalTokenPlace;
import org.sagebionetworks.web.client.presenter.DataAccessApprovalTokenPresenter;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.DataAccessApprovalTokenView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class DataAccessApprovalTokenPresenterTest {

  DataAccessApprovalTokenPresenter presenter;

  @Mock
  DataAccessApprovalTokenView mockView;

  @Mock
  DataAccessApprovalTokenPlace mockPlace;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  SynapseAlert mockSynAlert;

  @Mock
  PopupUtilsView mockPopupUtils;

  @Captor
  ArgumentCaptor<Callback> callbackCaptor;

  @Captor
  ArgumentCaptor<Place> placeCaptor;

  public static final String TEST_RESPONSE =
    "The submission contained 1 tokens, 0 of which were valid. More information about error would be here.";

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    presenter =
      new DataAccessApprovalTokenPresenter(
        mockView,
        mockPopupUtils,
        mockSynAlert,
        mockJsClient
      );
    AsyncMockStubber
      .callSuccessWith(TEST_RESPONSE)
      .when(mockJsClient)
      .submitNRGRDataAccessToken(anyString(), any(AsyncCallback.class));
  }

  @Test
  public void testConstruction() {
    verify(mockView).setSynAlert(mockSynAlert);
    verify(mockView).setPresenter(presenter);
  }

  @Test
  public void testSetPlace() {
    String initToken = "12345";
    when(mockPlace.toToken()).thenReturn(initToken);

    presenter.setPlace(mockPlace);

    verify(mockView).setAccessApprovalToken(initToken);
    verify(mockView).refreshHeader();
  }

  @Test
  public void testSubmit() {
    String token = "8762387";
    when(mockView.getAccessApprovalToken()).thenReturn(token);

    presenter.onSubmitToken();

    verify(mockSynAlert).clear();
    verify(mockView).setLoading(true);
    verify(mockJsClient)
      .submitNRGRDataAccessToken(eq(token), any(AsyncCallback.class));
    verify(mockView).setLoading(false);
    // should have called success
    verify(mockPopupUtils).showInfoDialog("", TEST_RESPONSE, null);
  }

  @Test
  public void testSubmitEmptyToken() {
    String token = "   ";
    when(mockView.getAccessApprovalToken()).thenReturn(token);

    presenter.onSubmitToken();

    verify(mockSynAlert)
      .showError(DataAccessApprovalTokenPresenter.EMPTY_TOKEN_ERROR_MESSAGE);
  }

  @Test
  public void testSubmitFailure() {
    when(mockView.getAccessApprovalToken()).thenReturn("1");
    Exception testEx = new Exception("unable to make service call");
    AsyncMockStubber
      .callFailureWith(testEx)
      .when(mockJsClient)
      .submitNRGRDataAccessToken(anyString(), any(AsyncCallback.class));

    presenter.onSubmitToken();

    verify(mockSynAlert).clear();
    verify(mockView).setLoading(true);
    verify(mockJsClient)
      .submitNRGRDataAccessToken(anyString(), any(AsyncCallback.class));
    verify(mockView).setLoading(false);
    // should have called failure
    verify(mockSynAlert).handleException(testEx);
  }
}
