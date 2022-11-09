package org.sagebionetworks.web.client.widget.table.modal.download;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

public class DownloadFilePageImpl implements DownloadFilePage {

  public static final String DOWNLOAD = "Download";
  // Injected dependencies
  DownloadFilePageView view;
  SynapseJavascriptClient jsClient;
  GWTWrapper gwtWrapper;

  // configured data.
  ModalPresenter presenter;
  String resultsFileHandleId;

  @Inject
  public DownloadFilePageImpl(
    DownloadFilePageView view,
    SynapseJavascriptClient jsClient,
    GWTWrapper gwtWrapper
  ) {
    super();
    this.view = view;
    this.gwtWrapper = gwtWrapper;
    this.jsClient = jsClient;
  }

  @Override
  public void onPrimary() {
    this.presenter.setLoading(true);
    // Get a pre-signed URL.
    jsClient.getTemporaryFileHandleURL(
      this.resultsFileHandleId,
      new AsyncCallback<String>() {
        @Override
        public void onSuccess(String url) {
          download(url);
        }

        @Override
        public void onFailure(Throwable caught) {
          presenter.setErrorMessage(caught.getMessage());
        }
      }
    );
  }

  public void download(String url) {
    gwtWrapper.assignThisWindowWith(url);
    presenter.onFinished();
  }

  @Override
  public void setModalPresenter(ModalPresenter presenter) {
    this.presenter = presenter;
    this.presenter.setPrimaryButtonText(DOWNLOAD);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  @Override
  public void configure(String resultsFileHandleId) {
    this.resultsFileHandleId = resultsFileHandleId;
  }
}
