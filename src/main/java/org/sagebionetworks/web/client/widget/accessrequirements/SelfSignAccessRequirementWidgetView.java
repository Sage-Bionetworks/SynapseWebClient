package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import org.sagebionetworks.web.client.widget.lazyload.SupportsLazyLoadInterface;

public interface SelfSignAccessRequirementWidgetView
  extends IsWidget, SupportsLazyLoadInterface {
  /**
   * Set the presenter.
   *
   * @param presenter
   */
  void setPresenter(Presenter presenter);

  void addStyleNames(String styleNames);

  void setWikiTermsWidget(Widget wikiWidget);

  void showApprovedHeading();

  void showUnapprovedHeading();

  void showSignTermsButton();

  void showGetCertifiedUI();

  void showGetProfileValidatedUI();

  void resetState();

  void setEditAccessRequirementWidget(IsWidget w);

  void setDeleteAccessRequirementWidget(IsWidget w);

  void setSubjectsWidget(IsWidget w);

  void setManageAccessWidget(IsWidget w);

  void showLoginButton();

  void hideControls();

  void setAccessRequirementID(String arID);
  void setAccessRequirementIDVisible(boolean visible);

  /**
   * Presenter interface
   */
  public interface Presenter {
    void onSignTerms();

    void onCertify();

    void onValidateProfile();
  }
}
