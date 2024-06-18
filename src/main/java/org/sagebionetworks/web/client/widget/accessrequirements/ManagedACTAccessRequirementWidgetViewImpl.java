package org.sagebionetworks.web.client.widget.accessrequirements;

import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import java.util.Collections;
import javax.annotation.Nullable;
import org.gwtbootstrap3.client.ui.Alert;
import org.gwtbootstrap3.client.ui.Button;
import org.gwtbootstrap3.client.ui.html.Div;
import org.gwtbootstrap3.client.ui.html.Span;
import org.gwtbootstrap3.client.ui.html.Text;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.RestrictableObjectDescriptor;
import org.sagebionetworks.repo.model.RestrictableObjectType;
import org.sagebionetworks.schema.adapter.JSONObjectAdapter;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.context.SynapseReactClientFullContextPropsProvider;
import org.sagebionetworks.web.client.jsinterop.AccessRequirementListProps;
import org.sagebionetworks.web.client.jsinterop.React;
import org.sagebionetworks.web.client.jsinterop.SRC;
import org.sagebionetworks.web.client.place.LoginPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.ReactComponent;

public class ManagedACTAccessRequirementWidgetViewImpl
  implements ManagedACTAccessRequirementWidgetView {

  @UiField
  Div approvedHeading;

  @UiField
  Div unapprovedHeading;

  @UiField
  SimplePanel wikiContainer;

  @UiField
  Alert requestSubmittedMessage;

  @UiField
  Alert requestApprovedMessage;

  @UiField
  Alert requestRejectedMessage;

  @UiField
  Button cancelRequestButton;

  @UiField
  Button updateRequestButton;

  @UiField
  Button requestAccessButton;

  @UiField
  Button loginButton;

  @UiField
  ReactComponent requestDataAccessWidget;

  @UiField
  Div editAccessRequirementContainer;

  @UiField
  Div reviewAccessRequestsContainer;

  @UiField
  Div manageAccessContainer;

  @UiField
  Div controlsContainer;

  @UiField
  Div teamSubjectsWidgetContainer;

  @UiField
  Div coveredEntitiesHeadingUI;

  @UiField
  Div entitySubjectsWidgetContainer;

  @UiField
  Div accessRequirementRelatedProjectsListContainer;

  @UiField
  Div synAlertContainer;

  @UiField
  Div requestSubmittedByOther;

  @UiField
  Div submitterUserBadgeContainer;

  @UiField
  Div cancelRequestButtonContainer;

  @UiField
  Div updateRequestButtonContainer;

  @UiField
  Div requestAccessButtonContainer;

  @UiField
  Div iduReportButtonContainer;

  @UiField
  Span expirationUI;

  @UiField
  Text expirationDateText;

  @UiField
  Div accessRequirementIDUI;

  @UiField
  InlineLabel accessRequirementIDField;

  @UiField
  Span accessRequirementDescription;

  @UiField
  Div subjectsDefinedByAnnotationsUI;

  @UiField
  Div subjectsDefinedInAccessRequirementUI;

  private final JSONObjectAdapter jsonObjectAdapter;
  private final SynapseReactClientFullContextPropsProvider propsProvider;
  Callback onAttachCallback;
  public static final String DEFAULT_AR_DESCRIPTION = "these data";

  public interface Binder
    extends UiBinder<Widget, ManagedACTAccessRequirementWidgetViewImpl> {}

  Widget w;
  Presenter presenter;

  @Inject
  public ManagedACTAccessRequirementWidgetViewImpl(
    Binder binder,
    GlobalApplicationState globalAppState,
    JSONObjectAdapter jsonObjectAdapter,
    SynapseReactClientFullContextPropsProvider propsProvider
  ) {
    this.w = binder.createAndBindUi(this);
    this.jsonObjectAdapter = jsonObjectAdapter;
    this.propsProvider = propsProvider;
    cancelRequestButton.addClickHandler(event -> {
      presenter.onCancelRequest();
    });
    updateRequestButton.addClickHandler(event -> {
      presenter.onRequestAccess();
    });
    requestAccessButton.addClickHandler(event -> {
      presenter.onRequestAccess();
    });
    w.addAttachHandler(event -> {
      if (event.isAttached()) {
        onAttachCallback.invoke();
      }
    });
    loginButton.addClickHandler(event -> {
      globalAppState
        .getPlaceChanger()
        .goTo(new LoginPlace(LoginPlace.LOGIN_TOKEN));
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
  public void setWikiTermsWidgetVisible(boolean visible) {
    wikiContainer.setVisible(visible);
  }

  @Override
  public void showApprovedHeading() {
    approvedHeading.setVisible(true);
  }

  @Override
  public void showUnapprovedHeading() {
    unapprovedHeading.setVisible(true);
  }

  @Override
  public void showCancelRequestButton() {
    cancelRequestButton.setVisible(true);
  }

  @Override
  public void showRequestAccessButton() {
    requestAccessButton.setVisible(true);
  }

  @Override
  public void showRequestApprovedMessage() {
    requestApprovedMessage.setVisible(true);
  }

  @Override
  public void showRequestRejectedMessage(String reason) {
    requestRejectedMessage.setText("Rejected : " + reason);
    requestRejectedMessage.setVisible(true);
  }

  @Override
  public void showRequestSubmittedMessage() {
    requestSubmittedMessage.setVisible(true);
  }

  @Override
  public void showUpdateRequestButton() {
    updateRequestButton.setVisible(true);
  }

  @Override
  public void resetState() {
    approvedHeading.setVisible(false);
    unapprovedHeading.setVisible(false);
    requestSubmittedMessage.setVisible(false);
    requestApprovedMessage.setVisible(false);
    requestRejectedMessage.setVisible(false);
    cancelRequestButton.setVisible(false);
    updateRequestButton.setVisible(false);
    requestAccessButton.setVisible(false);
    requestSubmittedByOther.setVisible(false);
    expirationUI.setVisible(false);
    loginButton.setVisible(false);
  }

  @Override
  public void setSubmitterUserBadge(IsWidget w) {
    submitterUserBadgeContainer.clear();
    submitterUserBadgeContainer.add(w);
  }

  @Override
  public void showRequestSubmittedByOtherUser() {
    requestSubmittedByOther.setVisible(true);
  }

  @Override
  public void setEditAccessRequirementWidget(IsWidget w) {
    editAccessRequirementContainer.clear();
    editAccessRequirementContainer.add(w);
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
  public void setVisible(boolean visible) {
    w.setVisible(visible);
  }

  @Override
  public void setManageAccessWidget(IsWidget w) {
    manageAccessContainer.clear();
    manageAccessContainer.add(w);
  }

  @Override
  public void setReviewAccessRequestsWidget(IsWidget w) {
    reviewAccessRequestsContainer.clear();
    reviewAccessRequestsContainer.add(w);
  }

  @Override
  public void setSynAlert(IsWidget w) {
    synAlertContainer.clear();
    synAlertContainer.add(w);
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
  public void setReviewAccessRequestsWidgetContainerVisible(boolean visible) {
    reviewAccessRequestsContainer.setVisible(visible);
  }

  @Override
  public void hideControls() {
    controlsContainer.setVisible(false);
  }

  @Override
  public void showExpirationDate(String dateString) {
    expirationDateText.setText(dateString);
    expirationUI.setVisible(true);
  }

  @Override
  public void showLoginButton() {
    loginButton.setVisible(true);
  }

  @Override
  public void setIDUReportButton(IsWidget w) {
    iduReportButtonContainer.clear();
    iduReportButtonContainer.add(w);
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
  public void setAccessRequirementName(String description) {
    if (DisplayUtils.isDefined(description)) {
      accessRequirementDescription.setText(description);
      accessRequirementDescription.addStyleName("boldText");
    } else {
      accessRequirementDescription.setText(DEFAULT_AR_DESCRIPTION);
      accessRequirementDescription.removeStyleName("boldText");
    }
  }

  @Override
  public void showRequestAccessModal(
    ManagedACTAccessRequirement accessRequirement,
    @Nullable RestrictableObjectDescriptor targetSubject
  ) {
    AccessRequirementListProps.Callback onHide = () -> {
      presenter.refreshApprovalState();
      hideRequestAccessModal();
    };
    String entityId = null;
    if (
      targetSubject != null &&
      targetSubject.getType() == RestrictableObjectType.ENTITY
    ) {
      entityId = targetSubject.getId();
    }
    AccessRequirementListProps props = AccessRequirementListProps.create(
      onHide,
      Collections.singletonList(accessRequirement),
      entityId
    );
    requestDataAccessWidget.render(
      React.createElementWithSynapseContext(
        SRC.SynapseComponents.AccessRequirementList,
        props,
        propsProvider.getJsInteropContextProps()
      )
    );
  }

  @Override
  public void setSubjectsDefinedByAnnotations(
    Boolean subjectsDefinedByAnnotations
  ) {
    boolean v = subjectsDefinedByAnnotations != null
      ? subjectsDefinedByAnnotations.booleanValue()
      : false;
    subjectsDefinedByAnnotationsUI.setVisible(v);
    subjectsDefinedInAccessRequirementUI.setVisible(!v);
  }

  public void hideRequestAccessModal() {
    requestDataAccessWidget.clear();
  }
}
