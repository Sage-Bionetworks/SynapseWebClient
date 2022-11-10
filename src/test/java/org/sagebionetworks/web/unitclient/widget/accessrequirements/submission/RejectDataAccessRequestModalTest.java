package org.sagebionetworks.web.unitclient.widget.accessrequirements.submission;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.widget.entity.act.RejectDataAccessRequestModal.ERROR_MESSAGE;
import static org.sagebionetworks.web.client.widget.entity.act.RejectDataAccessRequestModal.TEMPLATE_HEADER_SIGNATURE;
import static org.sagebionetworks.web.client.widget.entity.act.RejectDataAccessRequestModal.TEMPLATE_HEADER_THANKS;

import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.sagebionetworks.repo.model.FileEntity;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.act.RejectDataAccessRequestModal;
import org.sagebionetworks.web.client.widget.entity.act.RejectDataAccessRequestModalView;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

@RunWith(MockitoJUnitRunner.class)
public class RejectDataAccessRequestModalTest {

  RejectDataAccessRequestModal widget;

  @Mock
  RejectDataAccessRequestModalView mockView;

  @Mock
  PortalGinInjector mockGinInjector;

  @Mock
  SynapseProperties mockSynapseProperties;

  @Mock
  CallbackP<String> getReasonCallback;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  PresignedURLAsyncHandler mockPresignedURLAsyncHandler;

  @Mock
  RequestBuilderWrapper mockRequestBuilder;

  @Mock
  FileEntity mockFileEntity;

  @Mock
  FileResult mockFileResult;

  @Mock
  Response mockResponse;

  @Captor
  ArgumentCaptor<RequestCallback> requestCallbackCaptor;

  JSONObjectAdapter jsonObjectAdapter = new JSONObjectAdapterImpl();

  public static final String REASONS_FILE_SYN_ID = "syn1234";
  public static final String SELECTED_CHECKBOXES =
    "Must do this, and that, and the other thing.";
  public static final String CANNED_RESPONSE = "canned response";

  @Before
  public void setUp() throws Exception {
    when(mockView.getSelectedCheckboxText()).thenReturn(SELECTED_CHECKBOXES);
    when(
      mockSynapseProperties.getSynapseProperty(
        WebConstants.ACT_PROFILE_VALIDATION_REJECTION_REASONS_PROPERTY_KEY
      )
    )
      .thenReturn(REASONS_FILE_SYN_ID);
    when(mockGinInjector.getSynapseJavascriptClient()).thenReturn(mockJsClient);
    when(mockGinInjector.getPresignedURLAsyncHandler())
      .thenReturn(mockPresignedURLAsyncHandler);
    when(mockGinInjector.getRequestBuilder()).thenReturn(mockRequestBuilder);
    when(mockGinInjector.getJSONObjectAdapter()).thenReturn(jsonObjectAdapter);
    when(mockResponse.getText())
      .thenReturn("{\"reasons\":[\"test1\", \"test2\"]}");
    when(mockResponse.getStatusCode()).thenReturn(Response.SC_OK);

    AsyncMockStubber
      .callSuccessWith(mockFileEntity)
      .when(mockJsClient)
      .getEntity(anyString(), any(AsyncCallback.class));
    AsyncMockStubber
      .callSuccessWith(mockFileResult)
      .when(mockPresignedURLAsyncHandler)
      .getFileResult(
        any(FileHandleAssociation.class),
        any(AsyncCallback.class)
      );

    widget =
      new RejectDataAccessRequestModal(
        mockView,
        mockSynapseProperties,
        mockGinInjector
      );
  }

  private void verifyRequestBuilderCall() {
    try {
      verify(mockRequestBuilder)
        .sendRequest(anyString(), requestCallbackCaptor.capture());
      requestCallbackCaptor.getValue().onResponseReceived(null, mockResponse);
    } catch (RequestException e) {
      fail("request builder sendRequest failed");
    }
  }

  @Test
  public void testConstructor() {
    verify(mockView).setPresenter(widget);
  }

  @Test
  public void testShowOnSuccess() {
    // call
    widget.show(getReasonCallback);

    // verify/assert
    verify(mockJsClient).getEntity(anyString(), any(AsyncCallback.class));
    verify(mockPresignedURLAsyncHandler)
      .getFileResult(
        any(FileHandleAssociation.class),
        any(AsyncCallback.class)
      );
    verifyRequestBuilderCall();
    verify(mockView).clear();
    verify(mockView).show();

    // verify save onSave callback
    when(mockView.getValue()).thenReturn(CANNED_RESPONSE);

    widget.onSave();

    verify(mockView).hide();
    verify(getReasonCallback).invoke(CANNED_RESPONSE);
  }

  @Test
  public void testUpdateResponse() {
    widget.show(getReasonCallback);
    String exp =
      TEMPLATE_HEADER_THANKS + SELECTED_CHECKBOXES + TEMPLATE_HEADER_SIGNATURE;

    widget.updateResponse();

    verify(mockView).setValue(exp);
  }

  @Test
  public void testOnSaveNoText() {
    when(mockView.getValue()).thenReturn("");

    widget.onSave();

    verify(mockView).showError(ERROR_MESSAGE);
  }
}
