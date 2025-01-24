package org.sagebionetworks.web.client.presenter;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import org.sagebionetworks.web.client.PopupUtilsView;
import org.sagebionetworks.web.client.place.TrustCenterPlace;
import org.sagebionetworks.web.client.view.MapView;
import org.sagebionetworks.web.client.view.TrustCenterView;

public class TrustCenterPresenter
  extends AbstractActivity
  implements
    Presenter<org.sagebionetworks.web.client.place.TrustCenterPlace>,
    MapView.Presenter {

  TrustCenterView view;
  PopupUtilsView popupUtils;

  private static Map<String, String> documentKeyToGithubFilename =
    new HashMap<>();
  public static String REPO_OWNER = "Sage-Bionetworks";
  public static String REPO_NAME = "Sage-Governance-Documents";

  static {
    documentKeyToGithubFilename.put(
      TrustCenterPlace.TERMS_OF_SERVICE_KEY,
      "Terms.md"
    );
    documentKeyToGithubFilename.put(
      TrustCenterPlace.PRIVACY_POLICY_KEY,
      "privacy.md"
    );
    documentKeyToGithubFilename.put(TrustCenterPlace.COOKIES_KEY, "cookies.md");
    documentKeyToGithubFilename.put(
      TrustCenterPlace.SUBPROCESSORS_KEY,
      "subprocessors.md"
    );
  }

  @Inject
  public TrustCenterPresenter(TrustCenterView view, PopupUtilsView popupUtils) {
    this.view = view;
    this.popupUtils = popupUtils;
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    panel.setWidget(view.asWidget());
  }

  @Override
  public void setPlace(
    org.sagebionetworks.web.client.place.TrustCenterPlace place
  ) {
    String documentKey = place.getDocumentKey();
    String fileName = documentKeyToGithubFilename.get(documentKey);
    if (fileName == null) {
      popupUtils.showErrorMessage("Unrecognized document key: " + documentKey);
    } else {
      view.render(REPO_OWNER, REPO_NAME, fileName);
    }
  }

  @Override
  public String mayStop() {
    return null;
  }
}
