package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.ACCESSOR_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.ACCESS_REQUIREMENT_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.EXPIRES_BEFORE_PARAM;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.SUBMITTER_ID_PARAM;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Date;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupRequest;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupResponse;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.view.ACTAccessApprovalsView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.user.UserBadge;

public class ACTAccessApprovalsPresenter
  extends AbstractActivity
  implements
    Presenter<ACTAccessApprovalsPlace>, ACTAccessApprovalsView.Presenter {

  private ACTAccessApprovalsPlace place;
  private ACTAccessApprovalsView view;
  private PortalGinInjector ginInjector;
  private SynapseAlert synAlert;
  DataAccessClientAsync dataAccessClient;
  LoadMoreWidgetContainer loadMoreContainer;
  boolean isAccessRequirementVisible;
  public static final String HIDE_AR_TEXT = "Hide Access Requirement";
  public static final String SHOW_AR_TEXT = "Show Access Requirement";
  AccessorGroupRequest accessorGroupRequest;
  SynapseSuggestBox submitterSuggestWidget;
  UserBadge selectedSubmitterUserBadge;
  SynapseSuggestBox accessorSuggestWidget;
  UserBadge selectedAccessorUserBadge;

  Button showHideAccessRequirementButton;
  AccessRequirementWidget accessRequirementWidget;
  Callback refreshCallback;
  GlobalApplicationState globalAppState;
  String accessRequirementId;
  ArrayList<AccessorGroup> allExportData;
  boolean isLoadingAllData;

  @Inject
  public ACTAccessApprovalsPresenter(
    final ACTAccessApprovalsView view,
    SynapseAlert synAlert,
    PortalGinInjector ginInjector,
    LoadMoreWidgetContainer loadMoreContainer,
    final Button showHideAccessRequirementButton,
    DataAccessClientAsync dataAccessClient,
    SynapseSuggestBox accessorSuggestWidget,
    SynapseSuggestBox submitterSuggestWidget,
    UserGroupSuggestionProvider provider,
    UserBadge selectedSubmitterUserBadge,
    UserBadge selectedAccessorUserBadge,
    AccessRequirementWidget accessRequirementWidget,
    GlobalApplicationState globalAppState
  ) {
    this.view = view;
    this.synAlert = synAlert;
    this.ginInjector = ginInjector;
    this.dataAccessClient = dataAccessClient;
    fixServiceEntryPoint(dataAccessClient);
    this.loadMoreContainer = loadMoreContainer;
    this.selectedSubmitterUserBadge = selectedSubmitterUserBadge;
    this.showHideAccessRequirementButton = showHideAccessRequirementButton;
    this.accessRequirementWidget = accessRequirementWidget;
    this.globalAppState = globalAppState;
    this.submitterSuggestWidget = submitterSuggestWidget;
    this.selectedAccessorUserBadge = selectedAccessorUserBadge;
    this.accessorSuggestWidget = accessorSuggestWidget;
    submitterSuggestWidget.setSuggestionProvider(provider);
    submitterSuggestWidget.setTypeFilter(TypeFilter.USERS_ONLY);
    accessorSuggestWidget.setSuggestionProvider(provider);
    accessorSuggestWidget.setTypeFilter(TypeFilter.USERS_ONLY);

    isAccessRequirementVisible = false;
    showHideAccessRequirementButton.setText(SHOW_AR_TEXT);
    showHideAccessRequirementButton.setIcon(IconType.TOGGLE_RIGHT);
    view.setAccessRequirementUIVisible(false);
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
    view.setLoadMoreContainer(loadMoreContainer);
    view.setShowHideButton(showHideAccessRequirementButton);
    view.setAccessRequirementWidget(accessRequirementWidget);
    view.setSubmitterPickerWidget(submitterSuggestWidget.asWidget());
    view.setAccessorPickerWidget(accessorSuggestWidget);
    view.setSelectedSubmitterUserBadge(selectedSubmitterUserBadge.asWidget());
    view.setSelectedAccessorUserBadge(selectedAccessorUserBadge);

    view.setPresenter(this);
    submitterSuggestWidget.addItemSelectedHandler(suggestion -> {
      onSubmitterSelected(suggestion);
    });

    accessorSuggestWidget.addItemSelectedHandler(suggestion -> {
      onAccessorSelected(suggestion);
    });

    loadMoreContainer.configure(() -> {
      loadMore();
    });
    refreshCallback =
      () -> {
        loadData();
      };
  }

  @Override
  public void start(AcceptsOneWidget panel, EventBus eventBus) {
    // Install the view
    panel.setWidget(view);
  }

  @Override
  public void setPlace(ACTAccessApprovalsPlace place) {
    this.place = place;
    accessorGroupRequest = new AccessorGroupRequest();
    accessRequirementId = place.getParam(ACCESS_REQUIREMENT_ID_PARAM);
    String expireTime = place.getParam(EXPIRES_BEFORE_PARAM);
    String submitterId = place.getParam(SUBMITTER_ID_PARAM);
    String accessorId = place.getParam(ACCESSOR_ID_PARAM);
    if (expireTime != null) {
      Date expiresBeforeDate = new Date(Long.parseLong(expireTime));
      view.setExpiresBeforeDate(expiresBeforeDate);
      accessorGroupRequest.setExpireBefore(expiresBeforeDate);
    }

    synAlert.clear();
    boolean isAccessRequirementId = accessRequirementId != null;

    showHideAccessRequirementButton.setVisible(isAccessRequirementId);
    view.setClearAccessRequirementFilterButtonVisible(isAccessRequirementId);
    if (isAccessRequirementId) {
      accessorGroupRequest.setAccessRequirementId(accessRequirementId);
      accessRequirementWidget.configure(accessRequirementId, null);
    }
    accessorGroupRequest.setSubmitterId(submitterId);
    accessorGroupRequest.setAccessorId(accessorId);
    loadData();
  }

  public void loadData() {
    globalAppState.pushCurrentPlace(place);
    allExportData = new ArrayList<>();
    loadMoreContainer.clear();
    accessorGroupRequest.setNextPageToken(null);
    isLoadingAllData = false;
    view.resetExportButton();
    loadMore();
  }

  public void loadMore() {
    synAlert.clear();
    dataAccessClient.listAccessorGroup(
      accessorGroupRequest,
      new AsyncCallback<AccessorGroupResponse>() {
        @Override
        public void onFailure(Throwable caught) {
          synAlert.handleException(caught);
          loadMoreContainer.setIsMore(false);
        }

        public void onSuccess(AccessorGroupResponse response) {
          accessorGroupRequest.setNextPageToken(response.getNextPageToken());
          for (AccessorGroup group : response.getResults()) {
            AccessorGroupWidget w = ginInjector.getAccessorGroupWidget();
            w.configure(group);
            w.setOnRevokeCallback(refreshCallback);
            loadMoreContainer.add(w.asWidget());
          }
          boolean isMore = response.getNextPageToken() != null;
          loadMoreContainer.setIsMore(isMore);
          allExportData.addAll(response.getResults());
          // as soon as we reach the end of the results, automatically set up the export file
          if (!isMore) {
            view.export(allExportData);
          }
          if (isMore && isLoadingAllData) {
            loadMore();
          }
        }
      }
    );
  }

  @Override
  public void onClearExpireBeforeFilter() {
    accessorGroupRequest.setExpireBefore(null);
    view.setExpiresBeforeDate(null);
    place.removeParam(EXPIRES_BEFORE_PARAM);
    loadData();
  }

  @Override
  public void onClearSubmitterFilter() {
    accessorGroupRequest.setSubmitterId(null);
    place.removeParam(SUBMITTER_ID_PARAM);
    view.setSelectedSubmitterUserBadgeVisible(false);
    submitterSuggestWidget.clear();
    loadData();
  }

  @Override
  public void onClearAccessorFilter() {
    accessorGroupRequest.setAccessorId(null);
    place.removeParam(ACCESSOR_ID_PARAM);
    view.setSelectedAccessorUserBadgeVisible(false);
    accessorSuggestWidget.clear();
    loadData();
  }

  @Override
  public void onClearAccessRequirementFilter() {
    accessorGroupRequest.setAccessRequirementId(null);
    place.removeParam(ACCESS_REQUIREMENT_ID_PARAM);
    view.setAccessRequirementUIVisible(false);
    showHideAccessRequirementButton.setVisible(false);
    view.setClearAccessRequirementFilterButtonVisible(false);
    loadData();
  }

  @Override
  public void onExpiresBeforeDateSelected(Date date) {
    accessorGroupRequest.setExpireBefore(date);
    place.putParam(EXPIRES_BEFORE_PARAM, Long.toString(date.getTime()));
    loadData();
  }

  public void onSubmitterSelected(UserGroupSuggestion suggestion) {
    if (suggestion != null) {
      UserGroupHeader header = suggestion.getHeader();
      accessorGroupRequest.setSubmitterId(header.getOwnerId());
      place.putParam(SUBMITTER_ID_PARAM, header.getOwnerId());
      selectedSubmitterUserBadge.configure(header.getOwnerId());
      submitterSuggestWidget.clear();
      view.setSelectedSubmitterUserBadgeVisible(true);
      loadData();
    } else {
      onClearSubmitterFilter();
    }
  }

  public void onAccessorSelected(UserGroupSuggestion suggestion) {
    if (suggestion != null) {
      UserGroupHeader header = suggestion.getHeader();
      accessorGroupRequest.setAccessorId(header.getOwnerId());
      place.putParam(ACCESSOR_ID_PARAM, header.getOwnerId());
      selectedAccessorUserBadge.configure(header.getOwnerId());
      accessorSuggestWidget.clear();
      view.setSelectedAccessorUserBadgeVisible(true);
      loadData();
    } else {
      onClearAccessorFilter();
    }
  }

  public ACTAccessApprovalsPlace getPlace() {
    return place;
  }

  @Override
  public String mayStop() {
    return null;
  }

  @Override
  public void onExportData() {
    // gather all pages of data to set up export
    isLoadingAllData = true;
    loadMore();
  }
}
