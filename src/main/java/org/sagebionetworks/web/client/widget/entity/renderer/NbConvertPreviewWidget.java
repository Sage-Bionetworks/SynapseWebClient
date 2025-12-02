package org.sagebionetworks.web.client.widget.entity.renderer;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.SynapseJavascriptClient.ACCEPT;
import static org.sagebionetworks.web.client.SynapseJavascriptClient.AUTHORIZATION_HEADER;
import static org.sagebionetworks.web.client.SynapseJavascriptClient.BEARER_PREFIX;
import static org.sagebionetworks.web.shared.WebConstants.NBCONVERT_ENDPOINT_PROPERTY;
import static org.sagebionetworks.web.shared.WebConstants.REPO_SERVICE_URL_KEY;
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
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.asynch.PresignedURLAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;

public class NbConvertPreviewWidget implements IsWidget {

  String nbConvertEndpoint;
  public static final String HTML_PREFIX =
    "<html><head>" +
    "<link rel=\"stylesheet\" type=\"text/css\" href=\"css\\notebook.css\">" +
    "</head><body>";
  public static final String HTML_SUFFIX = "</body></html>";

  protected HtmlPreviewView view;
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
  AuthenticationController authController;

  @Inject
  public NbConvertPreviewWidget(
    HtmlPreviewView view,
    PresignedURLAsyncHandler presignedURLAsyncHandler,
    SynapseJSNIUtils jsniUtils,
    RequestBuilderWrapper requestBuilder,
    SynapseAlert synAlert,
    SynapseClientAsync synapseClient,
    PopupUtilsView popupUtils,
    SynapseProperties synapseProperties,
    GWTWrapper gwt,
    AuthenticationController authController
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
    this.authController = authController;
    view.setSynAlert(synAlert);
    if (friendlyMaxFileSize == null) {
      friendlyMaxFileSize =
        gwt.getFriendlySize(HtmlPreviewWidget.MAX_HTML_FILE_SIZE, true);
    }
    String repoUrl = synapseProperties.getSynapseProperty(REPO_SERVICE_URL_KEY);
    String stack = stackFromRepoEndpoint(repoUrl);
    nbConvertEndpoint =
      synapseProperties
        .getSynapseProperty(NBCONVERT_ENDPOINT_PROPERTY)
        .replaceAll("%s", stack);
  }

  protected String stackFromRepoEndpoint(String endpoint) {
    // deployed endpoint should be "repo-xxx.stack.sagebase.org"
    String stack = "dev";
    String[] parts = endpoint.split("\\.");
    if (parts.length == 4) {
      stack = parts[1];
    }
    return stack;
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
    view.setLoadingVisible(false);
    view.configure(createdBy, wrappedRawHtml);
  }

  public void setPresignedUrl(String url) {
    String encodedUrl = gwt.encodeQueryString(url);
    // use lambda endpoint to resolve ipynb file to html
    requestBuilder.configure(
      RequestBuilder.GET,
      nbConvertEndpoint + encodedUrl
    );
    requestBuilder.setHeader(ACCEPT, TEXT_HTML_CHARSET_UTF8);
    if (authController.isLoggedIn()) {
      requestBuilder.setHeader(
        AUTHORIZATION_HEADER,
        BEARER_PREFIX + authController.getCurrentUserAccessToken()
      );
    }
    try {
      requestBuilder.sendRequest(null, getRequestCallback());
    } catch (final Exception e) {
      view.setLoadingVisible(false);
      synAlert.handleException(e);
    }
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
