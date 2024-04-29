package org.sagebionetworks.web.client.widget.accessrequirements;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.AccessApproval;
import org.sagebionetworks.repo.model.ObjectType;
import org.sagebionetworks.repo.model.TermsOfUseAccessRequirement;
import org.sagebionetworks.repo.model.dataaccess.AccessRequirementStatus;
import org.sagebionetworks.repo.model.dataaccess.BasicAccessRequirementStatus;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.widget.asynch.IsACTMemberAsyncHandler;
import org.sagebionetworks.web.client.widget.entity.WikiPageWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.lazyload.LazyLoadHelper;
import org.sagebionetworks.web.shared.WikiPageKey;

public class TermsOfUseAccessRequirementWidget
  implements TermsOfUseAccessRequirementWidgetView.Presenter, IsWidget {

  private TermsOfUseAccessRequirementWidgetView view;
  SynapseClientAsync synapseClient;
  SynapseJavascriptClient jsClient;
  DataAccessClientAsync dataAccessClient;
  SynapseAlert synAlert;
  WikiPageWidget wikiPageWidget;
  TermsOfUseAccessRequirement ar;
  AuthenticationController authController;
  CreateAccessRequirementButton createAccessRequirementButton;
  TeamSubjectsWidget teamSubjectsWidget;
  EntitySubjectsWidget entitySubjectsWidget;
  AccessRequirementRelatedProjectsList accessRequirementRelatedProjectsList;
  LazyLoadHelper lazyLoadHelper;
  Callback refreshCallback;
  ReviewAccessorsButton manageAccessButton;
  IsACTMemberAsyncHandler isACTMemberAsyncHandler;

  @Inject
  public TermsOfUseAccessRequirementWidget(
    TermsOfUseAccessRequirementWidgetView view,
    AuthenticationController authController,
    DataAccessClientAsync dataAccessClient,
    SynapseClientAsync synapseClient,
    SynapseJavascriptClient jsClient,
    WikiPageWidget wikiPageWidget,
    SynapseAlert synAlert,
    TeamSubjectsWidget teamSubjectsWidget,
    EntitySubjectsWidget entitySubjectsWidget,
    AccessRequirementRelatedProjectsList accessRequirementRelatedProjectsList,
    CreateAccessRequirementButton createAccessRequirementButton,
    LazyLoadHelper lazyLoadHelper,
    ReviewAccessorsButton manageAccessButton,
    IsACTMemberAsyncHandler isACTMemberAsyncHandler
  ) {
    this.view = view;
    this.synapseClient = synapseClient;
    fixServiceEntryPoint(synapseClient);
    this.jsClient = jsClient;
    this.dataAccessClient = dataAccessClient;
    fixServiceEntryPoint(dataAccessClient);
    this.synAlert = synAlert;
    this.wikiPageWidget = wikiPageWidget;
    this.authController = authController;
    this.teamSubjectsWidget = teamSubjectsWidget;
    this.entitySubjectsWidget = entitySubjectsWidget;
    this.accessRequirementRelatedProjectsList =
      accessRequirementRelatedProjectsList;
    this.createAccessRequirementButton = createAccessRequirementButton;
    this.lazyLoadHelper = lazyLoadHelper;
    this.manageAccessButton = manageAccessButton;
    this.isACTMemberAsyncHandler = isACTMemberAsyncHandler;
    wikiPageWidget.setModifiedCreatedByHistoryVisible(false);
    view.setPresenter(this);
    view.setWikiTermsWidget(wikiPageWidget.asWidget());
    view.setEditAccessRequirementWidget(createAccessRequirementButton);
    view.setTeamSubjectsWidget(teamSubjectsWidget);
    view.setEntitySubjectsWidget(entitySubjectsWidget);
    view.setAccessRequirementRelatedProjectsList(
      accessRequirementRelatedProjectsList
    );
    view.setManageAccessWidget(manageAccessButton);
    Callback loadDataCallback = new Callback() {
      @Override
      public void invoke() {
        refreshApprovalState();
      }
    };

    lazyLoadHelper.configure(loadDataCallback, view);
  }

  public void setRequirement(
    final TermsOfUseAccessRequirement ar,
    Callback refreshCallback
  ) {
    this.ar = ar;
    this.refreshCallback = refreshCallback;
    jsClient.getRootWikiPageKey(
      ObjectType.ACCESS_REQUIREMENT.toString(),
      ar.getId().toString(),
      new AsyncCallback<String>() {
        @Override
        public void onFailure(Throwable caught) {
          view.setTerms(ar.getTermsOfUse());
          view.showTermsUI();
        }

        @Override
        public void onSuccess(String rootWikiId) {
          // get wiki terms
          WikiPageKey wikiKey = new WikiPageKey(
            ar.getId().toString(),
            ObjectType.ACCESS_REQUIREMENT.toString(),
            rootWikiId
          );
          wikiPageWidget.configure(wikiKey, false, null);
          view.showWikiTermsUI();
        }
      }
    );
    createAccessRequirementButton.configure(ar, refreshCallback);
    view.setAccessRequirementID(ar.getId().toString());
    isACTMemberAsyncHandler.isACTActionAvailable(isACT -> {
      view.setAccessRequirementIDVisible(isACT);
      view.setCoveredEntitiesHeadingVisible(isACT);
      // show the subjects defined by annotations UI if isACT and this flag is set
      view.setSubjectsDefinedByAnnotations(
        isACT && ar.getSubjectsDefinedByAnnotations()
      );
    });
    teamSubjectsWidget.configure(ar.getSubjectIds());
    entitySubjectsWidget.configure(ar.getSubjectIds());
    accessRequirementRelatedProjectsList.configure(ar.getId().toString());
    manageAccessButton.configure(ar);
    lazyLoadHelper.setIsConfigured();
  }

  public void setDataAccessSubmissionStatus(
    BasicAccessRequirementStatus status
  ) {
    // set up view based on DataAccessSubmission state
    if (status.getIsApproved()) {
      view.showApprovedHeading();
    } else {
      view.showUnapprovedHeading();
      view.showSignTermsButton();
    }
  }

  public void showAnonymous() {
    view.showUnapprovedHeading();
    view.showLoginButton();
  }

  public void refreshApprovalState() {
    view.resetState();
    if (!authController.isLoggedIn()) {
      showAnonymous();
      return;
    }
    dataAccessClient.getAccessRequirementStatus(
      ar.getId().toString(),
      new AsyncCallback<AccessRequirementStatus>() {
        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
        }

        @Override
        public void onSuccess(AccessRequirementStatus status) {
          setDataAccessSubmissionStatus((BasicAccessRequirementStatus) status);
        }
      }
    );
  }

  @Override
  public void onSignTerms() {
    // create the self-signed access approval, then update this object
    synAlert.clear();
    AsyncCallback<AccessApproval> callback = new AsyncCallback<
      AccessApproval
    >() {
      @Override
      public void onFailure(Throwable t) {
        synAlert.handleException(t);
      }

      @Override
      public void onSuccess(AccessApproval result) {
        refreshCallback.invoke();
      }
    };
    AccessApproval approval = new AccessApproval();
    approval.setAccessorId(authController.getCurrentUserPrincipalId());
    approval.setRequirementId(ar.getId());
    synapseClient.createAccessApproval(approval, callback);
  }

  public void addStyleNames(String styleNames) {
    view.addStyleNames(styleNames);
  }

  @Override
  public Widget asWidget() {
    return view.asWidget();
  }

  public void hideControls() {
    view.hideControls();
  }
}
