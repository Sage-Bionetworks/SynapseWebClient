package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.SynapseJavascriptClient.ACCEPT;
import static org.sagebionetworks.web.shared.WebConstants.NBCONVERT_ENDPOINT_PROPERTY;
import static org.sagebionetworks.web.shared.WebConstants.TEXT_HTML_CHARSET_UTF8;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.file.FileHandle;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.file.FileResult;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class NbConvertPreviewWidget
  implements IsWidget, NbConvertPreviewView.Presenter {

  String nbConvertEndpoint;
  public static final String HTML_PREFIX =
    "<html><head>" +
    "<link rel=\"stylesheet\" type=\"text/css\" href=\"css\\notebook.css\">" +
    "</head><body>";
  public static final String HTML_SUFFIX = "</body></html>";

  protected NbConvertPreviewView view;
  protected PresignedURLAsyncHandler presignedURLAsyncHandler;
  protected FileHandleAssociation fha;
  protected SynapseAlert synAlert;
  protected RequestBuilderWrapper requestBuilder;
  protected SynapseJSNIUtils jsniUtils;
  protected String createdBy;
  protected SynapseClientAsync synapseClient;
  protected PopupUtilsView popupUtils;
  protected GWTWrapper gwt;
  public static String friendlyMaxFileSize = null;

  @Inject
  public NbConvertPreviewWidget(
    NbConvertPreviewView view,
    PresignedURLAsyncHandler presignedURLAsyncHandler,
    SynapseJSNIUtils jsniUtils,
    RequestBuilderWrapper requestBuilder,
    SynapseAlert synAlert,
    SynapseClientAsync synapseClient,
    PopupUtilsView popupUtils,
    SynapseProperties synapseProperties,
    GWTWrapper gwt
  ) {
    this.view = view;
    this.presignedURLAsyncHandler = presignedURLAsyncHandler;
    this.jsniUtils = jsniUtils;
    this.requestBuilder = requestBuilder;
    this.synAlert = synAlert;
    this.synapseClient = synapseClient;
    fixServiceEntryPoint(synapseClient);
    this.popupUtils = popupUtils;
    this.gwt = gwt;
    view.setSynAlert(synAlert);
    view.setPresenter(this);
    if (friendlyMaxFileSize == null) {
      friendlyMaxFileSize =
        gwt.getFriendlySize(HtmlPreviewWidget.MAX_HTML_FILE_SIZE, true);
    }
    nbConvertEndpoint =
      synapseProperties.getSynapseProperty(NBCONVERT_ENDPOINT_PROPERTY);
  }

  public void configure(String synapseId, FileHandle fileHandle) {
    this.createdBy = fileHandle.getCreatedBy();
    fha = new FileHandleAssociation();
    fha.setAssociateObjectId(synapseId);
    fha.setAssociateObjectType(FileHandleAssociateType.FileEntity);
    fha.setFileHandleId(fileHandle.getId());
    if (
      fileHandle.getContentSize() != null &&
      fileHandle.getContentSize() < HtmlPreviewWidget.MAX_HTML_FILE_SIZE
    ) {
      refreshContent();
    } else {
      view.setLoadingVisible(false);
      synAlert.showError(
        "The preview was not shown because the size (" +
        gwt.getFriendlySize(fileHandle.getContentSize().doubleValue(), true) +
        ") exceeds the maximum preview size (" +
        friendlyMaxFileSize +
        ")"
      );
    }
  }

  public void refreshContent() {
    if (fha != null) {
      synAlert.clear();
      view.setLoadingVisible(true);
      presignedURLAsyncHandler.getFileResult(
        fha,
        new AsyncCallback<FileResult>() {
          @Override
          public void onSuccess(FileResult fileResult) {
            setPresignedUrl(fileResult.getPreSignedURL());
          }

          @Override
          public void onFailure(Throwable ex) {
            view.setLoadingVisible(false);
            synAlert.handleException(ex);
          }
        }
      );
    }
  }

  public void renderHTML(String rawHtml) {
    String wrappedRawHtml = HTML_PREFIX + rawHtml + HTML_SUFFIX;
    synapseClient.isUserAllowedToRenderHTML(
      createdBy,
      new AsyncCallback<Boolean>() {
        @Override
        public void onFailure(Throwable caught) {
          view.setLoadingVisible(false);
          showSanitizedHtml();
          jsniUtils.consoleError(caught.getMessage());
        }

        @Override
        public void onSuccess(Boolean trustedUser) {
          view.setLoadingVisible(false);
          if (trustedUser) {
            view.setHtml(wrappedRawHtml);
          } else {
            showSanitizedHtml();
          }
        }

        private void showSanitizedHtml() {
          // is the sanitized version the same as the original??
          String sanitizedHtml = jsniUtils.sanitizeHtml(wrappedRawHtml);
          if (wrappedRawHtml.equals(sanitizedHtml)) {
            view.setHtml(wrappedRawHtml);
          } else {
            view.setHtml(sanitizedHtml);
            view.setRawHtml(wrappedRawHtml);
          }
        }
      }
    );
  }

  public void setPresignedUrl(String url) {
    String encodedUrl = gwt.encodeQueryString(url);
    // use lambda endpoint to resolve ipynb file to html
    requestBuilder.configure(
      RequestBuilder.GET,
      nbConvertEndpoint + encodedUrl
    );
    requestBuilder.setHeader(ACCEPT, TEXT_HTML_CHARSET_UTF8);
    try {
      requestBuilder.sendRequest(null, getRequestCallback());
    } catch (final Exception e) {
      view.setLoadingVisible(false);
      synAlert.handleException(e);
    }
  }

  @Override
  public void onShowFullContent() {
    // in this case (ipynb), to show the full content they need to download the ipynb file and view it
    // locally
    presignedURLAsyncHandler.getFileResult(
      fha,
      new AsyncCallback<FileResult>() {
        @Override
        public void onSuccess(FileResult fileResult) {
          view.openInNewWindow(fileResult.getPreSignedURL());
        }

        @Override
        public void onFailure(Throwable ex) {
          view.setLoadingVisible(false);
          synAlert.handleException(ex);
        }
      }
    );
  }

  protected RequestCallback getRequestCallback() {
    return new RequestCallback() {
      @Override
      public void onResponseReceived(Request request, Response response) {
        int statusCode = response.getStatusCode();
        if (statusCode == Response.SC_OK) {
          renderHTML(response.getText());
        } else {
          onError(
            null,
            new IllegalArgumentException(
              "Unable to retrieve. Reason: " + response.getStatusText()
            )
          );
        }
      }

      @Override
      public void onError(Request request, Throwable exception) {
        view.setLoadingVisible(false);
        synAlert.handleException(exception);
      }
    };
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }
}
