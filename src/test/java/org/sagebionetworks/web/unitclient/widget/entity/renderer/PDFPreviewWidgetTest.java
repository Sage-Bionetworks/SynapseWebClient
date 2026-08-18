package org.sagebionetworks.web.unitclient.widget.entity.renderer;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.widget.entity.renderer.IFrameView;
import org.sagebionetworks.web.client.widget.entity.renderer.PDFPreviewWidget;

public class PDFPreviewWidgetTest {

  PDFPreviewWidget widget;

  @Mock
  IFrameView mockView;

  @Mock
  SynapseJSNIUtils mockJSNIUtils;

  @Mock
  GWTWrapper mockGWT;

  @Captor
  ArgumentCaptor<String> stringCaptor;

  @Captor
  ArgumentCaptor<AsyncCallback<String>> blobUrlCallbackCaptor;

  @Mock
  FileHandle mockFileHandle;

  private static final String URL = "fileHandleServlet?filehandleid=x";
  private static final String BLOB_URL = "blob:http://localhost/abc-123";
  public static final String FRIENDLY_FILE_SIZE = "a friendly file size";

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    when(mockGWT.getFriendlySize(anyDouble(), anyBoolean()))
      .thenReturn(FRIENDLY_FILE_SIZE);
    widget = new PDFPreviewWidget(mockView, mockJSNIUtils, mockGWT);
    when(
      mockJSNIUtils.getFileHandleAssociationUrl(
        anyString(),
        any(FileHandleAssociateType.class),
        anyString()
      )
    )
      .thenReturn(URL);
  }

  @Test
  public void testConstructor() {
    verify(mockView).addAttachHandler(any());
  }

  @Test
  public void testAsWidget() {
    widget.asWidget();
    verify(mockView).asWidget();
  }

  @Test
  public void testConfigure() {
    int parentOffsetHeight = 200;
    String synId = "syn1996";
    String fileHandleId = "1812";

    when(mockView.getParentOffsetHeight()).thenReturn(parentOffsetHeight);
    when(mockFileHandle.getId()).thenReturn(fileHandleId);
    when(mockFileHandle.getContentSize()).thenReturn(1L);

    widget.configure(synId, mockFileHandle);

    verify(mockJSNIUtils)
      .getFileHandleAssociationUrl(
        synId,
        FileHandleAssociateType.FileEntity,
        fileHandleId
      );
    // the file handle url is fetched, and the view is not configured until the fetch resolves
    verify(mockJSNIUtils)
      .fetchAsBlobUrl(eq(URL), blobUrlCallbackCaptor.capture());
    verify(mockView, never()).configure(anyString(), anyInt());

    // once the blob url is available, the view is configured with it
    blobUrlCallbackCaptor.getValue().onSuccess(BLOB_URL);
    verify(mockView).configure(BLOB_URL, parentOffsetHeight);
  }

  @Test
  public void testConfigureDefaultHeight() {
    // when the parent height is unavailable, the default height is used instead
    when(mockView.getParentOffsetHeight()).thenReturn(0);
    when(mockFileHandle.getId()).thenReturn("1812");
    when(mockFileHandle.getContentSize()).thenReturn(1L);

    widget.configure("syn1996", mockFileHandle);

    verify(mockJSNIUtils)
      .fetchAsBlobUrl(anyString(), blobUrlCallbackCaptor.capture());
    blobUrlCallbackCaptor.getValue().onSuccess(BLOB_URL);

    verify(mockView).configure(BLOB_URL, PDFPreviewWidget.DEFAULT_HEIGHT_PX);
  }

  @Test
  public void testConfigureFetchFailure() {
    String errorMessage = "network is down";
    when(mockFileHandle.getId()).thenReturn("1812");
    when(mockFileHandle.getContentSize()).thenReturn(1L);

    widget.configure("syn1996", mockFileHandle);

    verify(mockJSNIUtils)
      .fetchAsBlobUrl(anyString(), blobUrlCallbackCaptor.capture());
    blobUrlCallbackCaptor
      .getValue()
      .onFailure(new RuntimeException(errorMessage));

    verify(mockView, never()).configure(anyString(), anyInt());
    verify(mockView).showError(stringCaptor.capture());
    assertTrue(stringCaptor.getValue().contains(errorMessage));
  }

  @Test
  public void testMaxFileSizeExceeded() {
    when(mockFileHandle.getId()).thenReturn("23");
    when(mockFileHandle.getContentSize())
      .thenReturn(
        Double.valueOf(PDFPreviewWidget.MAX_PDF_FILE_SIZE + 10).longValue()
      );

    widget.configure("syn23", mockFileHandle);

    // no fetch is attempted
    verify(mockJSNIUtils, never()).fetchAsBlobUrl(anyString(), any());
    // verify an error was shown, relating to the file size
    verify(mockView).showError(stringCaptor.capture());
    assertTrue(stringCaptor.getValue().contains(FRIENDLY_FILE_SIZE));
  }
}
