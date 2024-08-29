package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.sagebionetworks.web.client.RequestBuilderWrapper;
import org.sagebionetworks.web.client.place.TrustCenterPlace;
import org.sagebionetworks.web.client.view.MapView;
import org.sagebionetworks.web.client.view.SynapseStandaloneWikiView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.shared.WebConstants;

public class TrustCenterPresenter
  extends AbstractActivity
  implements
    Presenter<org.sagebionetworks.web.client.place.TrustCenterPlace>,
    MapView.Presenter {

  SynapseStandaloneWikiView view;
  RequestBuilderWrapper requestBuilder;
  SynapseAlert synAlert;

  private static Map<String, String> documentKeyToGithubUrl = new HashMap<>();

  static {
    documentKeyToGithubUrl.put(
      TrustCenterPlace.TERMS_OF_SERVICE_KEY,
      "https://raw.githubusercontent.com/Sage-Bionetworks/Sage-Governance-Documents/main/Terms.md"
    );
    documentKeyToGithubUrl.put(
      TrustCenterPlace.PRIVACY_POLICY_KEY,
      "https://raw.githubusercontent.com/Sage-Bionetworks/Sage-Governance-Documents/main/privacy.md"
    );
    documentKeyToGithubUrl.put(
      TrustCenterPlace.COOKIES_KEY,
      "https://raw.githubusercontent.com/Sage-Bionetworks/Sage-Governance-Documents/main/cookies.md"
    );
    documentKeyToGithubUrl.put(
      TrustCenterPlace.SUBPROCESSORS_KEY,
      "https://raw.githubusercontent.com/Sage-Bionetworks/Sage-Governance-Documents/main/subprocessors.md"
    );
  }

  @Inject
  public TrustCenterPresenter(
    SynapseStandaloneWikiView view,
    RequestBuilderWrapper requestBuilder,
    SynapseAlert synAlert
  ) {
    this.view = view;
    this.requestBuilder = requestBuilder;
    this.synAlert = synAlert;
    view.setSynAlert(synAlert);
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
  }

  @Override
  public void setPlace(
    org.sagebionetworks.web.client.place.TrustCenterPlace place
  ) {
    synAlert.clear();
    String documentKey = place.getDocumentKey();
    String documentUrl = documentKeyToGithubUrl.get(documentKey);
    if (documentUrl == null) {
      synAlert.showError("Unrecognized document key: " + documentKey);
    } else {
      showFileContent(documentUrl);
    }
  }

  public void showFileContent(String url) {
    requestBuilder.configure(RequestBuilder.GET, url);
    requestBuilder.setHeader(
      WebConstants.CONTENT_TYPE,
      WebConstants.TEXT_PLAIN_CHARSET_UTF8
    );
    try {
      requestBuilder.sendRequest(
        null,
        new RequestCallback() {
          @Override
          public void onResponseReceived(Request request, Response response) {
            int statusCode = response.getStatusCode();
            if (statusCode == Response.SC_OK) {
              String md = response.getText();
              view.configure(md);
            } else {
              onError(
                null,
                new IllegalArgumentException(
                  "Unable to retrieve file content " +
                  url +
                  ". Reason: " +
                  response.getStatusText()
                )
              );
            }
          }

          @Override
          public void onError(Request request, Throwable exception) {
            synAlert.handleException(exception);
          }
        }
      );
    } catch (final Exception e) {
      synAlert.handleException(e);
    }
  }

  @Override
  public String mayStop() {
    return null;
  }
}
