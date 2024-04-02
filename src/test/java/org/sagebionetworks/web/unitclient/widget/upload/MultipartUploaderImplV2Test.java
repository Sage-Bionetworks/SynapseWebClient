package org.sagebionetworks.web.unitclient.widget.upload;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.sagebionetworks.web.client.ContentTypeUtils.fixDefaultContentType;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.logical.shared.HasAttachHandlers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.util.ContentTypeUtils;
import org.sagebionetworks.web.client.DateTimeUtils;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.ProgressCallback;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.jsinterop.Promise;
import org.sagebionetworks.web.client.jsinterop.Promise.FunctionParam;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.FileUploadComplete;
import org.sagebionetworks.web.client.jsinterop.SRC.SynapseClient.IsCancelled;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.upload.MultipartUploaderImplV2;
import org.sagebionetworks.web.client.widget.upload.ProgressingFileUploadHandler;
import org.sagebionetworks.web.client.widget.upload.SRCUploadFileWrapper;
import org.sagebionetworks.web.shared.WebConstants;
import org.sagebionetworks.web.shared.exceptions.RestServiceException;

public class MultipartUploaderImplV2Test {

  @Mock
  AuthenticationController mockAuth;

  @Mock
  ProgressingFileUploadHandler mockHandler;

  @Mock
  SynapseJSNIUtils synapseJsniUtils;

  @Mock
  GWTWrapper gwt;

  MultipartUploaderImplV2 uploader;

  Long storageLocationId = 9090L;

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
  SynapseProperties mockSynapseProperties;

  @Mock
  Promise<FileUploadComplete> mockPromise;

  @Captor
  ArgumentCaptor<IsCancelled> isCancelledCaptor;

  @Mock
  FileUploadComplete mockFileUploadComplete;

  @Captor
  ArgumentCaptor<FunctionParam> promiseHandlerCaptor;

  public static final String UPLOAD_ID = "39282";
  public static final String RESULT_FILE_HANDLE_ID = "999999";
  public static final double FILE_SIZE = 9281;
  public static final String FILE_NAME = "file.txt";
  public static final String CONTENT_TYPE = "text/plain";
  public static final long defaultStorageLocationId = 1L;

  @Before
  public void before() throws Exception {
    MockitoAnnotations.initMocks(this);

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

    when(
      mockSynapseProperties.getSynapseProperty(
        WebConstants.DEFAULT_STORAGE_ID_PROPERTY_KEY
      )
    ).thenReturn(String.valueOf(defaultStorageLocationId));

    uploader = new MultipartUploaderImplV2(
      mockAuth,
      gwt,
      synapseJsniUtils,
      mockDateTimeUtils,
      mockSRCUploadFileWrapper,
      mockSynapseProperties
    );

    when(mockView.isAttached()).thenReturn(true);
    mockFileUploadComplete.fileHandleId = RESULT_FILE_HANDLE_ID;
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

    verify(mockPromise).then(promiseHandlerCaptor.capture());
    FunctionParam thenHandler = promiseHandlerCaptor.getValue();

    thenHandler.exec(mockFileUploadComplete);

    // the handler should get the id.
    verify(mockHandler).uploadSuccess(RESULT_FILE_HANDLE_ID);
  }

  @Test
  public void testDirectUploadEmptyStorageLocationId() throws Exception {
    uploader.uploadFile(
      FILE_NAME,
      CONTENT_TYPE,
      mockFileBlob,
      mockHandler,
      null,
      mockView
    );

    verify(mockPromise).then(promiseHandlerCaptor.capture());
    FunctionParam thenHandler = promiseHandlerCaptor.getValue();

    thenHandler.exec(mockFileUploadComplete);

    verify(mockSRCUploadFileWrapper).uploadFile(
      anyString(),
      anyString(),
      any(),
      eq((int) defaultStorageLocationId),
      anyString(),
      any(),
      any()
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

    verify(mockSRCUploadFileWrapper).uploadFile(
      anyString(),
      anyString(),
      any(),
      anyInt(),
      anyString(),
      any(),
      isCancelledCaptor.capture()
    );
    IsCancelled isCancelled = isCancelledCaptor.getValue();
    assertEquals(false, isCancelled.isCancelled());

    // user cancels the upload
    uploader.cancelUpload();

    assertEquals(true, isCancelled.isCancelled());
    // never updated the handler because the upload has been canceled (can't verify the abort(), since
    // xhr is a js object).
    verifyZeroInteractions(mockHandler);
  }

  @Test
  public void testStartMultipartUploadFailure() throws Exception {
    String error = "failed";
    uploader.uploadFile(
      FILE_NAME,
      CONTENT_TYPE,
      mockFileBlob,
      mockHandler,
      storageLocationId,
      mockView
    );

    verify(mockPromise).catch_(promiseHandlerCaptor.capture());
    FunctionParam errorHandler = promiseHandlerCaptor.getValue();

    errorHandler.exec(error);

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
