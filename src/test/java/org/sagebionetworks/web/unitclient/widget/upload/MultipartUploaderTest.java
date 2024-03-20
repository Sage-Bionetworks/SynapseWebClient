package org.sagebionetworks.web.unitclient.widget.upload;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.ContentTypeUtils.fixDefaultContentType;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.file.MultipartUploadRequest;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.DisplayConstants;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.cookie.CookieProvider;
import org.sagebionetworks.web.client.jsinterop.Promise;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.upload.MultipartUploaderImpl;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.SRCUploadFileWrapper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;
import org.sagebionetworks.web.test.helper.CallbackMockStubber;

public class MultipartUploaderTest {

  @Mock
  AuthenticationController mockAuth;

  @Mock
  ProgressingFileUploadHandler mockHandler;

  @Mock
  SynapseJSNIUtils synapseJsniUtils;

  @Mock
  GWTWrapper gwt;

  MultipartUploaderImpl uploader;
  String MD5;
  String partMd5;

  String[] fileNames;
  Long storageLocationId = 9090L;

  @Mock
  SynapseJavascriptClient mockJsClient;

  @Mock
  CookieProvider mockCookies;

  @Mock
  HasAttachHandlers mockView;

  @Mock
  DateTimeUtils mockDateTimeUtils;

  @Captor
  ArgumentCaptor<ProgressCallback> progressCaptor;

  @Mock
  JavaScriptObject mockFileBlob;

  @Mock
  SRCUploadFileWrapper mockSRCUploadFileWrapper;

  @Mock
  Promise mockPromise;

  public static final String UPLOAD_ID = "39282";
  public static final String RESULT_FILE_HANDLE_ID = "999999";
  public static final double FILE_SIZE = 9281;
  public static final String FILE_NAME = "file.txt";
  public static final String CONTENT_TYPE = "text/plain";

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);

    // direct upload
    // by default, do not support direct upload (direct upload tests will turn on)
    when(
      synapseJsniUtils.getContentType(any(JavaScriptObject.class), anyInt())
    ).thenReturn("image/png");

    when(
      mockSRCUploadFileWrapper.uploadFile(
        anyString(),
        anyString(),
        any(),
        anyInt(),
        anyString(),
        any(),
        any()
      )
    ).thenReturn(mockPromise);
    when(synapseJsniUtils.getFileSize(any(JavaScriptObject.class))).thenReturn(
      FILE_SIZE
    );
    // fire the timer
    CallbackMockStubber.invokeCallback()
      .when(gwt)
      .scheduleExecution(any(Callback.class), anyInt());
    when(gwt.createXMLHttpRequest()).thenReturn(null);

    String[] fileNames = { "newFile.txt" };
    when(
      synapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))
    ).thenReturn(fileNames);

    String file1 = "file1.txt";
    fileNames = new String[] { file1 };
    when(
      synapseJsniUtils.getMultipleUploadFileNames(any(JavaScriptObject.class))
    ).thenReturn(fileNames);

    uploader = new MultipartUploaderImpl(
      mockAuth,
      gwt,
      synapseJsniUtils,
      mockCookies,
      mockDateTimeUtils,
      mockSRCUploadFileWrapper
    );

    when(mockView.isAttached()).thenReturn(true);
  }

  @Test
  public void testDirectUploadFolder() throws Exception {
    uploader.uploadFile(
      FILE_NAME,
      CONTENT_TYPE,
      mockFileBlob,
      mockHandler,
      storageLocationId,
      mockView
    );
    verify(mockHandler).uploadFailed(DisplayConstants.MD5_CALCULATION_ERROR);
  }

  @Test
  public void testDirectUploadEmptyFile() throws Exception {
    when(synapseJsniUtils.getFileSize(any(JavaScriptObject.class))).thenReturn(
      0.0
    );
    uploader.uploadFile(
      FILE_NAME,
      CONTENT_TYPE,
      mockFileBlob,
      mockHandler,
      storageLocationId,
      mockView
    );
    verify(mockJsClient, never()).startMultipartUpload(
      any(MultipartUploadRequest.class),
      anyBoolean(),
      any(AsyncCallback.class)
    );
    verify(mockHandler).uploadSuccess(null);
  }

  @Test
  public void testDirectUploadSinglePart() throws Exception {
    uploader.uploadFile(
      FILE_NAME,
      CONTENT_TYPE,
      mockFileBlob,
      mockHandler,
      storageLocationId,
      mockView
    );
    // the handler should get the id.
    verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
  }

  @Test
  public void testUploadCanceled() throws Exception {
    uploader.uploadFile(
      FILE_NAME,
      CONTENT_TYPE,
      mockFileBlob,
      mockHandler,
      storageLocationId,
      mockView
    );

    // user cancels the upload
    uploader.cancelUpload();

    // never updated the handler because the upload has been canceled (can't verify the abort(), since
    // xhr is a js object).
    verifyZeroInteractions(mockHandler);
  }

  @Test
  public void testStartMultipartUploadFailure() throws Exception {
    String error = "failed";
    AsyncMockStubber.callFailureWith(new IllegalArgumentException(error))
      .when(mockJsClient)
      .startMultipartUpload(
        any(MultipartUploadRequest.class),
        anyBoolean(),
        any(AsyncCallback.class)
      );
    uploader.uploadFile(
      FILE_NAME,
      CONTENT_TYPE,
      mockFileBlob,
      mockHandler,
      storageLocationId,
      mockView
    );

    verify(mockHandler).uploadFailed(error);
  }

  @Test
  public void testFixingDefaultContentType() throws RestServiceException {
    String inputFilename = "file.R";
    String inputContentType = "foo/bar";

    // if the content type coming from the browser field is set,
    // then this method should never override it
    assertEquals(
      inputContentType,
      fixDefaultContentType(inputContentType, inputFilename)
    );

    // but if the field reports a null or empty content type, then this method should fix it
    inputContentType = "";
    assertEquals(
      ContentTypeUtils.PLAIN_TEXT,
      fixDefaultContentType(inputContentType, inputFilename)
    );

    inputContentType = null;
    assertEquals(
      ContentTypeUtils.PLAIN_TEXT,
      fixDefaultContentType(inputContentType, inputFilename)
    );

    // should fix tab delimited files too
    inputFilename = "file.tab";
    assertEquals(
      WebConstants.TEXT_TAB_SEPARATED_VALUES,
      fixDefaultContentType(inputContentType, inputFilename)
    );

    inputFilename = "file.tsv";
    assertEquals(
      WebConstants.TEXT_TAB_SEPARATED_VALUES,
      fixDefaultContentType(inputContentType, inputFilename)
    );

    // should fix CSV files as well
    inputFilename = "file.CSV";
    assertEquals(
      WebConstants.TEXT_COMMA_SEPARATED_VALUES,
      fixDefaultContentType(inputContentType, inputFilename)
    );

    // should fix text files as well
    inputFilename = "file.TXT";
    assertEquals(
      ContentTypeUtils.PLAIN_TEXT,
      fixDefaultContentType(inputContentType, inputFilename)
    );

    // should workflow files
    assertEquals(
      ContentTypeUtils.PLAIN_TEXT,
      fixDefaultContentType(inputContentType, "test.wDL")
    );
    assertEquals(
      ContentTypeUtils.PLAIN_TEXT,
      fixDefaultContentType(inputContentType, "test.Cwl")
    );

    // test default
    inputContentType = null;
    inputFilename = "";
    assertEquals(
      ContentTypeUtils.APPLICATION_OCTET_STREAM,
      fixDefaultContentType(inputContentType, inputFilename)
    );
  }
}
