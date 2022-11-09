package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.ACCESSOR_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.ACCESS_REQUIREMENT_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace.STATE_FILTER_PARAM;
import static org.sagebionetworks.web.client.widget.accessrequirements.createaccessrequirement.CreateManagedACTAccessRequirementStep2.DAY_IN_MS;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.AccessRequirement;
import org.sagebionetworks.repo.model.ManagedACTAccessRequirement;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.dataaccess.Submission;
import org.sagebionetworks.repo.model.dataaccess.SubmissionOrder;
import org.sagebionetworks.repo.model.dataaccess.SubmissionPage;
import org.sagebionetworks.repo.model.dataaccess.SubmissionState;
import org.sagebionetworks.repo.model.file.FileHandleAssociateType;
import org.sagebionetworks.repo.model.file.FileHandleAssociation;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.GWTWrapper;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.place.ACTDataAccessSubmissionsPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ACTDataAccessSubmissionsView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.FileHandleWidget;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.ManagedACTAccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.SubjectsWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.submission.ACTDataAccessSubmissionWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class ACTDataAccessSubmissionsPresenter
  extends AbstractActivity
  implements
    Presenter<ACTDataAccessSubmissionsPlace>,
    ACTDataAccessSubmissionsView.Presenter {

  private SubmissionState stateFilter;
  private ACTDataAccessSubmissionsPlace place;
  private ACTDataAccessSubmissionsView view;
  private PortalGinInjector ginInjector;
  private SynapseAlert synAlert;
  SynapseJavascriptClient jsClient;
  LoadMoreWidgetContainer loadMoreContainer;
  ManagedACTAccessRequirementWidget actAccessRequirementWidget;
  boolean isAccessRequirementVisible;
  public static final String HIDE_AR_TEXT = "Hide Access Requirement";
  public static final String SHOW_AR_TEXT = "Show Access Requirement";
  public static final String INVALID_AR_ID = "Invalid Access Requirement ID";
  String actAccessRequirementId;
  FileHandleWidget ducTemplateFileHandleWidget;
  List<String> states;
  boolean isSortedAsc;
  String nextPageToken;
  String accessorId;
  private ManagedACTAccessRequirement actAccessRequirement;
  private SubjectsWidget subjectsWidget;
  DateTimeFormat dateFormat;
  Callback refreshCallback;
  SynapseSuggestBox accessorSuggestWidget;
  UserGroupSuggestionProvider provider;
  UserBadge selectedAccessorUserBadge;

  @Inject
  public ACTDataAccessSubmissionsPresenter(
    final ACTDataAccessSubmissionsView view,
    SynapseAlert synAlert,
    PortalGinInjector ginInjector,
    LoadMoreWidgetContainer loadMoreContainer,
    ManagedACTAccessRequirementWidget actAccessRequirementWidget,
    final Button showHideAccessRequirementButton,
    FileHandleWidget ducTemplateFileHandleWidget,
    SynapseJavascriptClient jsClient,
    SubjectsWidget subjectsWidget,
    GWTWrapper gwt,
    SynapseSuggestBox accessorSuggestWidget,
    UserGroupSuggestionProvider provider,
    UserBadge selectedAccessorUserBadge
  ) {
    this.view = view;
    this.synAlert = synAlert;
    this.ginInjector = ginInjector;
    this.jsClient = jsClient;
    this.loadMoreContainer = loadMoreContainer;
    this.actAccessRequirementWidget = actAccessRequirementWidget;
    actAccessRequirementWidget.setReviewAccessRequestsVisible(false);
    this.subjectsWidget = subjectsWidget;
    this.accessorSuggestWidget = accessorSuggestWidget;
    this.selectedAccessorUserBadge = selectedAccessorUserBadge;
    this.ducTemplateFileHandleWidget = ducTemplateFileHandleWidget;
    dateFormat = gwt.getDateTimeFormat(PredefinedFormat.DATE_FULL);
    states = new ArrayList<String>();
    for (SubmissionState state : SubmissionState.values()) {
      states.add(state.toString());
    }
    view.setStates(states);
    accessorSuggestWidget.setSuggestionProvider(provider);
    accessorSuggestWidget.setTypeFilter(TypeFilter.USERS_ONLY);

    isAccessRequirementVisible = false;
    showHideAccessRequirementButton.setText(SHOW_AR_TEXT);
    showHideAccessRequirementButton.setIcon(IconType.TOGGLE_RIGHT);
    view.setAccessRequirementUIVisible(false);
    view.setAccessorPickerWidget(accessorSuggestWidget);
    view.setSelectedAccessorUserBadge(selectedAccessorUserBadge);
    accessorSuggestWidget.addItemSelectedHandler(suggestion -> {
      onAccessorSelected(suggestion);
    });

    showHideAccessRequirementButton.addClickHandler(event -> {
      isAccessRequirementVisible = !isAccessRequirementVisible;
      String buttonText = isAccessRequirementVisible
        ? HIDE_AR_TEXT
        : SHOW_AR_TEXT;
      showHideAccessRequirementButton.setText(buttonText);
      showHideAccessRequirementButton.setIcon(
        isAccessRequirementVisible
          ? IconType.TOGGLE_DOWN
          : IconType.TOGGLE_RIGHT
      );
      view.setAccessRequirementUIVisible(isAccessRequirementVisible);
    });
    view.setSynAlert(synAlert);
    view.setAccessRequirementWidget(actAccessRequirementWidget);
    view.setLoadMoreContainer(loadMoreContainer);
    view.setShowHideButton(showHideAccessRequirementButton);
    view.setSubjectsWidget(subjectsWidget);
    view.setPresenter(this);

    loadMoreContainer.configure(
      new Callback() {
        @Override
        public void invoke() {
          loadMore();
        }
      }
    );
    refreshCallback =
      new Callback() {
        @Override
        public void invoke() {
          loadData();
        }
      };
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
  }

  @Override
  public void setPlace(ACTDataAccessSubmissionsPlace place) {
    this.place = place;
    isSortedAsc = false;
    actAccessRequirementId = place.getParam(ACCESS_REQUIREMENT_ID_PARAM);
    accessorId = place.getParam(ACCESSOR_ID_PARAM);
    synAlert.clear();
    view.setProjectedExpirationDateVisible(false);
    if (actAccessRequirementId != null) {
      jsClient.getAccessRequirement(
        actAccessRequirementId,
        new AsyncCallback<AccessRequirement>() {
          @Override
          public void onFailure(Throwable caught) {
            synAlert.showError(INVALID_AR_ID);
          }

          @Override
          public void onSuccess(AccessRequirement requirement) {
            if (requirement instanceof ManagedACTAccessRequirement) {
              actAccessRequirement = (ManagedACTAccessRequirement) requirement;

              refreshProjectedExpiration();
              if (actAccessRequirement.getDucTemplateFileHandleId() != null) {
                FileHandleAssociation fha = new FileHandleAssociation();
                fha.setAssociateObjectType(
                  FileHandleAssociateType.AccessRequirementAttachment
                );
                fha.setAssociateObjectId(
                  actAccessRequirement.getId().toString()
                );
                fha.setFileHandleId(
                  actAccessRequirement.getDucTemplateFileHandleId()
                );
                ducTemplateFileHandleWidget.configure(fha);
              }
              view.setAreOtherAttachmentsRequired(
                actAccessRequirement.getAreOtherAttachmentsRequired()
              );
              if (actAccessRequirement.getExpirationPeriod() != null) {
                view.setExpirationPeriod(
                  actAccessRequirement.getExpirationPeriod() / DAY_IN_MS
                );
              }

              view.setIsCertifiedUserRequired(
                actAccessRequirement.getIsCertifiedUserRequired()
              );
              view.setIsDUCRequired(actAccessRequirement.getIsDUCRequired());
              view.setIsIDURequired(actAccessRequirement.getIsIDURequired());
              view.setIsIDUPublic(actAccessRequirement.getIsIDUPublic());
              view.setIsIRBApprovalRequired(
                actAccessRequirement.getIsIRBApprovalRequired()
              );
              view.setIsValidatedProfileRequired(
                actAccessRequirement.getIsValidatedProfileRequired()
              );

              actAccessRequirementWidget.setRequirement(
                actAccessRequirement,
                refreshCallback
              );
              subjectsWidget.configure(actAccessRequirement.getSubjectIds());

              loadData();
            } else {
              synAlert.showError(
                INVALID_AR_ID +
                ": wrong type - " +
                requirement.getClass().getName()
              );
            }
          }
        }
      );
    } else {
      synAlert.showError(INVALID_AR_ID);
    }
  }

  public void refreshProjectedExpiration() {
    Long expirationPeriod = actAccessRequirement.getExpirationPeriod();
    if (expirationPeriod != null && expirationPeriod > 0) {
      Date expirationDate = new Date(new Date().getTime() + expirationPeriod);
      view.setProjectedExpirationDate(dateFormat.format(expirationDate));
      view.setProjectedExpirationDateVisible(true);
    }
  }

  public void loadData() {
    loadMoreContainer.clear();
    nextPageToken = null;
    loadMore();
  }

  public void loadMore() {
    loadMoreContainer.setIsProcessing(true);
    synAlert.clear();
    // ask for data access submissions once call is available, and create a widget to render.
    jsClient.getDataAccessSubmissions(
      actAccessRequirementId,
      accessorId,
      nextPageToken,
      stateFilter,
      SubmissionOrder.CREATED_ON,
      isSortedAsc,
      new AsyncCallback<SubmissionPage>() {
        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
          loadMoreContainer.setIsMore(false);
          loadMoreContainer.setIsProcessing(false);
        }

        public void onSuccess(SubmissionPage submissionPage) {
          nextPageToken = submissionPage.getNextPageToken();
          for (Submission submission : submissionPage.getResults()) {
            // create a new row for each data access submission.
            ACTDataAccessSubmissionWidget w = ginInjector.getACTDataAccessSubmissionWidget();
            w.configure(submission);
            w.setDucColumnVisible(actAccessRequirement.getIsDUCRequired());
            w.setIrbColumnVisible(
              actAccessRequirement.getIsIRBApprovalRequired()
            );
            w.setOtherAttachmentsColumnVisible(
              actAccessRequirement.getAreOtherAttachmentsRequired()
            );
            loadMoreContainer.add(w.asWidget());
          }
          loadMoreContainer.setIsMore(nextPageToken != null);
          loadMoreContainer.setIsProcessing(false);
        }
      }
    );
  }

  @Override
  public void onClearStateFilter() {
    stateFilter = null;
    place.removeParam(STATE_FILTER_PARAM);
    view.setSelectedStateText("");
    loadData();
  }

  @Override
  public void onClearAccessorFilter() {
    accessorId = null;
    place.removeParam(ACCESSOR_ID_PARAM);
    view.setSelectedAccessorUserBadgeVisible(false);
    accessorSuggestWidget.clear();
    loadData();
  }

  public void onAccessorSelected(UserGroupSuggestion suggestion) {
    if (suggestion != null) {
      UserGroupHeader header = suggestion.getHeader();
      accessorId = header.getOwnerId();
      place.putParam(ACCESSOR_ID_PARAM, header.getOwnerId());
      selectedAccessorUserBadge.configure(header.getOwnerId());
      accessorSuggestWidget.clear();
      view.setSelectedAccessorUserBadgeVisible(true);
      loadData();
    } else {
      onClearAccessorFilter();
    }
  }

  public ACTDataAccessSubmissionsPlace getPlace() {
    return place;
  }

  @Override
  public String mayStop() {
    return null;
  }

  @Override
  public void onStateSelected(String selectedState) {
    stateFilter = SubmissionState.valueOf(selectedState.toUpperCase());
    place.putParam(STATE_FILTER_PARAM, selectedState);
    view.setSelectedStateText(selectedState);
    loadData();
  }

  @Override
  public void onCreatedOnClick() {
    isSortedAsc = !isSortedAsc;
    loadData();
  }
}
