package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.ACCESS_REQUIREMENT_ID_PARAM;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.EXPIRES_BEFORE_PARAM;
import static org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace.SUBMITTER_ID_PARAM;

import java.util.Date;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroup;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupRequest;
import org.sagebionetworks.repo.model.dataaccess.AccessorGroupResponse;
import org.sagebionetworks.web.client.DataAccessClientAsync;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.place.ACTAccessApprovalsPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ACTAccessApprovalsView;
import org.sagebionetworks.web.client.widget.Button;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.accessrequirements.AccessRequirementWidget;
import org.sagebionetworks.web.client.widget.accessrequirements.approval.AccessorGroupWidget;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.user.UserBadge;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ACTAccessApprovalsPresenter extends AbstractActivity implements Presenter<ACTAccessApprovalsPlace>, ACTAccessApprovalsView.Presenter {
	private ACTAccessApprovalsPlace place;
	private ACTAccessApprovalsView view;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	DataAccessClientAsync dataAccessClient;
	LoadMoreWidgetContainer loadMoreContainer;
	boolean isAccessRequirementVisible;
	public static final String HIDE_AR_TEXT = "Hide Access Requirement";
	public static final String SHOW_AR_TEXT = "Show Access Requirement";
	public static final String INVALID_AR_ID = "Invalid Access Requirement ID";
	AccessorGroupRequest accessorGroupRequest;
	SynapseSuggestBox peopleSuggestWidget;
	private UserBadge selectedUserBadge;
	Button showHideAccessRequirementButton;
	AccessRequirementWidget accessRequirementWidget;
	Callback refreshCallback;
	@Inject
	public ACTAccessApprovalsPresenter(
			final ACTAccessApprovalsView view,
			SynapseAlert synAlert,
			PortalGinInjector ginInjector,
			LoadMoreWidgetContainer loadMoreContainer,
			final Button showHideAccessRequirementButton,
			DataAccessClientAsync dataAccessClient,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider,
			UserBadge selectedUserBadge,
			AccessRequirementWidget accessRequirementWidget
			) {
		this.view = view;
		this.synAlert = synAlert;
		this.ginInjector = ginInjector;
		this.dataAccessClient = dataAccessClient;
		this.loadMoreContainer = loadMoreContainer;
		this.selectedUserBadge = selectedUserBadge;
		this.showHideAccessRequirementButton = showHideAccessRequirementButton;
		this.accessRequirementWidget = accessRequirementWidget;
		peopleSuggestWidget.setSuggestionProvider(provider);
		isAccessRequirementVisible = false;
		showHideAccessRequirementButton.setText(SHOW_AR_TEXT);
		showHideAccessRequirementButton.setIcon(IconType.TOGGLE_RIGHT);
		view.setAccessRequirementUIVisible(false);
		showHideAccessRequirementButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				isAccessRequirementVisible = !isAccessRequirementVisible;
				String buttonText = isAccessRequirementVisible ? HIDE_AR_TEXT : SHOW_AR_TEXT;
				showHideAccessRequirementButton.setText(buttonText);
				showHideAccessRequirementButton.setIcon(isAccessRequirementVisible ? IconType.TOGGLE_DOWN : IconType.TOGGLE_RIGHT);
				view.setAccessRequirementUIVisible(isAccessRequirementVisible);
			}
		});
		view.setSynAlert(synAlert);
		view.setLoadMoreContainer(loadMoreContainer);
		view.setShowHideButton(showHideAccessRequirementButton);
		view.setAccessRequirementWidget(accessRequirementWidget);
		view.setUserPickerWidget(peopleSuggestWidget.asWidget());
		view.setSelectedUserBadge(selectedUserBadge.asWidget());
		view.setPresenter(this);
		peopleSuggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			@Override
			public void invoke(SynapseSuggestion suggestion) {
				onUserSelected((UserGroupSuggestion)suggestion);
			}
		});
		loadMoreContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
			}
		});
		refreshCallback = new Callback() {
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
	public void setPlace(ACTAccessApprovalsPlace place) {
		this.place = place;
		accessorGroupRequest = new AccessorGroupRequest();
		String actAccessRequirementIdString = place.getParam(ACCESS_REQUIREMENT_ID_PARAM);
		String expireTime = place.getParam(EXPIRES_BEFORE_PARAM);
		String submitterId = place.getParam(SUBMITTER_ID_PARAM);
		if (expireTime != null) {
			Date expiresBeforeDate = new Date(Long.parseLong(expireTime));
			view.setExpiresBeforeDate(expiresBeforeDate);
			accessorGroupRequest.setExpireBefore(expiresBeforeDate);
		}
		
		synAlert.clear();
		boolean isAccessRequirementId = actAccessRequirementIdString != null;
		
		view.setAccessRequirementUIVisible(isAccessRequirementId);
		showHideAccessRequirementButton.setVisible(isAccessRequirementId);
		if (isAccessRequirementId) {
			accessorGroupRequest.setAccessRequirementId(actAccessRequirementIdString);
			accessRequirementWidget.configure(actAccessRequirementIdString);
		}
		accessorGroupRequest.setSubmitterId(submitterId);
		loadData();
	}
	
	public void loadData() {
		loadMoreContainer.clear();
		accessorGroupRequest.setNextPageToken(null);
		loadMore();
	}

	public void loadMore() {
		synAlert.clear();
		dataAccessClient.listAccessorGroup(accessorGroupRequest, new AsyncCallback<AccessorGroupResponse>() {
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
				loadMoreContainer.setIsMore(response.getNextPageToken() != null);
			};
		});
	}
	
	@Override
	public void onClearExpireBeforeFilter() {
		accessorGroupRequest.setExpireBefore(null);
		view.setExpiresBeforeDate(null);
		place.removeParam(EXPIRES_BEFORE_PARAM);
		loadData();
	}
	
	@Override
	public void onClearUserFilter() {
		accessorGroupRequest.setSubmitterId(null);
		place.removeParam(SUBMITTER_ID_PARAM);
		view.setSelectedUserBadgeVisible(false);
		peopleSuggestWidget.clear();
		loadData();	
	}
	

	@Override
	public void onClearAccessRequirementFilter() {
		accessorGroupRequest.setAccessRequirementId(null);
		place.removeParam(ACCESS_REQUIREMENT_ID_PARAM);
		view.setAccessRequirementUIVisible(false);
		showHideAccessRequirementButton.setVisible(false);
		loadData();	
	}
	
	@Override
	public void onExpiresBeforeDateSelected(Date date) {
		accessorGroupRequest.setExpireBefore(date);
		place.putParam(EXPIRES_BEFORE_PARAM, Long.toString(date.getTime()));
		loadData();
	}
	
	public void onUserSelected(UserGroupSuggestion suggestion) {
		if(suggestion != null) {
			UserGroupHeader header = suggestion.getHeader();
			accessorGroupRequest.setSubmitterId(header.getOwnerId());
			place.putParam(SUBMITTER_ID_PARAM, header.getOwnerId());
			selectedUserBadge.configure(header.getOwnerId());
			peopleSuggestWidget.clear();
			view.setSelectedUserBadgeVisible(true);
			loadData();
		} else {
			onClearUserFilter();
		}
	}
	
	public ACTAccessApprovalsPlace getPlace() {
		return place;
	}
	
	@Override
    public String mayStop() {
        return null;
    }
}
