package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.BlockQuote;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.Callback;

public class TermsOfUseAccessRequirementWidgetViewImpl
  implements TermsOfUseAccessRequirementWidgetView {

  @UiField
  Div approvedHeading;

  @UiField
  Div unapprovedHeading;

  @UiField
  SimplePanel wikiContainer;

  @UiField
  BlockQuote wikiTermsUI;

  @UiField
  InlineLabel accessRequirementIDField;

  @UiField
  Div accessRequirementIDUI;

  @UiField
  BlockQuote termsUI;

  @UiField
  HTML terms;

  @UiField
  Button signTermsButton;

  @UiField
  Button loginButton;

  @UiField
  Div editAccessRequirementContainer;

  @UiField
  Div deleteAccessRequirementContainer;

  @UiField
  Div teamSubjectsWidgetContainer;

  @UiField
  Div coveredEntitiesHeadingUI;

  @UiField
  Div entitySubjectsWidgetContainer;

  @UiField
  Div accessRequirementRelatedProjectsListContainer;

  @UiField
  Div manageAccessContainer;

  @UiField
  Alert approvedAlert;

  @UiField
  Div controlsContainer;

  Callback onAttachCallback;

  public interface Binder
    extends UiBinder<Widget, TermsOfUseAccessRequirementWidgetViewImpl> {}

  Widget w;
  Presenter presenter;

  @Inject
  public TermsOfUseAccessRequirementWidgetViewImpl(
    Binder binder,
    GlobalApplicationState globalAppState
  ) {
    this.w = binder.createAndBindUi(this);
    signTermsButton.addClickHandler(event -> {
      presenter.onSignTerms();
    });
    loginButton.addClickHandler(event -> {
      globalAppState
        .getPlaceChanger()
        .goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
    });
    w.addAttachHandler(event -> {
      if (event.isAttached()) {
        onAttachCallback.invoke();
      }
    });
  }

  @Override
  public void addStyleNames(String styleNames) {
    w.addStyleName(styleNames);
  }

  @Override
  public void setPresenter(final Presenter presenter) {
    this.presenter = presenter;
  }

  @Override
  public Widget asWidget() {
    return w;
  }

  @Override
  public void setWikiTermsWidget(Widget wikiWidget) {
    wikiContainer.setWidget(wikiWidget);
  }

  @Override
  public void setTerms(String arText) {
    terms.setHTML(arText);
  }

  @Override
  public void setAccessRequirementID(String arID) {
    accessRequirementIDField.setText(arID);
  }

  @Override
  public void setAccessRequirementIDVisible(boolean visible) {
    accessRequirementIDUI.setVisible(visible);
  }

  @Override
  public void showTermsUI() {
    termsUI.setVisible(true);
  }

  @Override
  public void showWikiTermsUI() {
    wikiTermsUI.setVisible(true);
  }

  @Override
  public void showApprovedHeading() {
    approvedHeading.setVisible(true);
    approvedAlert.setVisible(true);
  }

  @Override
  public void showUnapprovedHeading() {
    unapprovedHeading.setVisible(true);
  }

  @Override
  public void showSignTermsButton() {
    signTermsButton.setVisible(true);
  }

  @Override
  public void resetState() {
    approvedAlert.setVisible(false);
    approvedHeading.setVisible(false);
    unapprovedHeading.setVisible(false);
    signTermsButton.setVisible(false);
    loginButton.setVisible(false);
  }

  @Override
  public void setEditAccessRequirementWidget(IsWidget w) {
    editAccessRequirementContainer.clear();
    editAccessRequirementContainer.add(w);
  }

  @Override
  public void setDeleteAccessRequirementWidget(IsWidget w) {
    deleteAccessRequirementContainer.clear();
    deleteAccessRequirementContainer.add(w);
  }

  @Override
  public void setTeamSubjectsWidget(IsWidget w) {
    teamSubjectsWidgetContainer.clear();
    teamSubjectsWidgetContainer.add(w);
  }

  @Override
  public void setEntitySubjectsWidget(IsWidget w) {
    entitySubjectsWidgetContainer.clear();
    entitySubjectsWidgetContainer.add(w);
  }

  @Override
  public void setCoveredEntitiesHeadingVisible(boolean visible) {
    coveredEntitiesHeadingUI.setVisible(visible);
  }

  @Override
  public void setAccessRequirementRelatedProjectsList(IsWidget w) {
    accessRequirementRelatedProjectsListContainer.clear();
    accessRequirementRelatedProjectsListContainer.add(w);
  }

  @Override
  public void setOnAttachCallback(Callback onAttachCallback) {
    this.onAttachCallback = onAttachCallback;
  }

  @Override
  public boolean isInViewport() {
    return DisplayUtils.isInViewport(w);
  }

  @Override
  public boolean isAttached() {
    return w.isAttached();
  }

  @Override
  public void setManageAccessWidget(IsWidget w) {
    manageAccessContainer.clear();
    manageAccessContainer.add(w);
  }

  @Override
  public void showLoginButton() {
    loginButton.setVisible(true);
  }

  @Override
  public void hideControls() {
    controlsContainer.setVisible(false);
  }
}
