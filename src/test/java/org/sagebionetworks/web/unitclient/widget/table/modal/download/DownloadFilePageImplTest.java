package org.sagebionetworks.web.unitclient.widget.table.modal.download;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadFilePageImpl;
import org.sagebionetworks.web.client.widget.table.modal.download.DownloadFilePageView;
import org.sagebionetworks.web.client.widget.table.modal.wizard.ModalPage.ModalPresenter;
import org.sagebionetworks.web.test.helper.AsyncMockStubber;

public class DownloadFilePageImplTest {

  ModalPresenter mockPresenter;
  DownloadFilePageView mockView;
  SynapseJavascriptClient mockJsClient;
  GWTWrapper mockGWTWrapper;
  DownloadFilePageImpl page;
  String fileHandleId;

  @Before
  public void before() {
    mockPresenter = Mockito.mock(ModalPresenter.class);
    mockView = Mockito.mock(DownloadFilePageView.class);
    mockJsClient = Mockito.mock(SynapseJavascriptClient.class);
    mockGWTWrapper = Mockito.mock(GWTWrapper.class);
    page = new DownloadFilePageImpl(mockView, mockJsClient, mockGWTWrapper);
    fileHandleId = "6789";
    page.configure(fileHandleId);
  }

  @Test
  public void testSetModalPresenter() {
    page.setModalPresenter(mockPresenter);
    verify(mockPresenter).setPrimaryButtonText(DownloadFilePageImpl.DOWNLOAD);
  }

  @Test
  public void testOnPrimaryFailure() {
    page.setModalPresenter(mockPresenter);
    String error = "an error";
    AsyncMockStubber
      .callFailureWith(new Throwable(error))
      .when(mockJsClient)
      .getTemporaryFileHandleURL(anyString(), any(AsyncCallback.class));
    page.onPrimary();
    verify(mockPresenter).setLoading(true);
    verify(mockPresenter, never()).onFinished();
    verify(mockPresenter).setErrorMessage(error);
  }

  @Test
  public void testOnPrimarySuccess() {
    page.setModalPresenter(mockPresenter);
    String url = "a URL";
    AsyncMockStubber
      .callSuccessWith(url)
      .when(mockJsClient)
      .getTemporaryFileHandleURL(anyString(), any(AsyncCallback.class));
    page.onPrimary();
    verify(mockPresenter).setLoading(true);
    verify(mockGWTWrapper).assignThisWindowWith(url);
    verify(mockPresenter).onFinished();
    verify(mockPresenter, never()).setErrorMessage(anyString());
  }
}
