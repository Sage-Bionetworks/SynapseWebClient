package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.place.ACTPlace;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ACTView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.user.UserBadge;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ACTPresenter extends AbstractActivity implements ACTView.Presenter, Presenter<ACTPlace> {
	private ACTPlace place;
	private ACTView view;
	private UserProfileClientAsync userProfileClient;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	private GlobalApplicationState globalAppState;
	SynapseSuggestBox peopleSuggestWidget;
	private VerificationStateEnum stateFilter;
	private Long submitterIdFilter;
	private UserBadge selectedUserBadge;
	public static Long LIMIT = 100L;
	List<String> states;
	
	@Inject
	public ACTPresenter(ACTView view,
			UserProfileClientAsync userProfileClient,
			SynapseAlert synAlert,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalAppState,
			UserBadge selectedUserBadge) {
		this.view = view;
		this.userProfileClient = userProfileClient;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		this.ginInjector = ginInjector;
		this.globalAppState = globalAppState;
		this.selectedUserBadge = selectedUserBadge;
		peopleSuggestWidget.setSuggestionProvider(provider);
		
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		states = new ArrayList<String>();
		for (VerificationStateEnum state : VerificationStateEnum.values()) {
			states.add(state.toString());
		}
		view.setStates(states);
		view.setUserPickerWidget(peopleSuggestWidget.asWidget());
		view.setSelectedUserBadge(selectedUserBadge.asWidget());
		view.setSelectedStateText("");
		peopleSuggestBox.addItemSelectedHandler(new CallbackP<SynapseSuggestion>() {
			@Override
			public void invoke(SynapseSuggestion suggestion) {
				onUserSelected((UserGroupSuggestion)suggestion);
			}
		});
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
		peopleSuggestWidget.setPlaceholderText("Filter by user...");
		loadData();
	}
	
	public void loadData() {
		loadData(0L);
	}
	
	@Override
	public void loadData(final Long offset) {
		view.clearRows();
		synAlert.clear();
		globalAppState.pushCurrentPlace(place);
		userProfileClient.listVerificationSubmissions(stateFilter, submitterIdFilter, LIMIT, offset, new AsyncCallback<VerificationPagedResults>() {
			@Override
			public void onSuccess(VerificationPagedResults results) {
				boolean isACT = true;
				boolean isModal = false;
				for (VerificationSubmission submission : results.getResults()) {
					VerificationSubmissionWidget w = ginInjector.getVerificationSubmissionWidget();
					w.configure(submission, isACT, isModal);
					view.addRow(w.asWidget());
					w.show();
				}
				List<PaginationEntry> entries = PaginationUtil.getPagination(results.getTotalNumberOfResults().intValue(), offset.intValue(), ACTPresenter.LIMIT.intValue(), 10);
				view.updatePagination(entries);
			}
			@Override
			public void onFailure(Throwable caught) {
				synAlert.handleException(caught);
			}
		});
	}
	
	
	@Override
	public void setPlace(ACTPlace place) {
		this.place = place;
		this.view.setPresenter(this);
		String stateFilterParam = place.getParam(ACTPlace.STATE_FILTER_PARAM);
		view.setSelectedStateText("");
		peopleSuggestWidget.clear();
		view.setSelectedUserBadgeVisible(false);
		
		if (stateFilterParam != null) {
			stateFilter = VerificationStateEnum.valueOf(stateFilterParam);
			view.setSelectedStateText(stateFilterParam);
		}
		String submitterIdFilterParam =  place.getParam(ACTPlace.SUBMITTER_ID_FILTER_PARAM);
		if (submitterIdFilterParam != null) {
			submitterIdFilter = Long.parseLong(submitterIdFilterParam);
			selectedUserBadge.configure(submitterIdFilterParam);
			view.setSelectedUserBadgeVisible(true);
		}
	}
	
	@Override
	public void onStateSelected(String selectedState) {
		stateFilter = VerificationStateEnum.valueOf(selectedState);
		place.putParam(ACTPlace.STATE_FILTER_PARAM, selectedState);
		view.setSelectedStateText(selectedState);
		loadData();
	}
	
	@Override
	public void onClearStateFilter() {
		stateFilter = null;
		place.removeParam(ACTPlace.STATE_FILTER_PARAM);
		view.setSelectedStateText("");
		loadData();
	}
	
	public void onUserSelected(UserGroupSuggestion suggestion) {
		if(suggestion != null) {
			UserGroupHeader header = suggestion.getHeader();
			submitterIdFilter = Long.parseLong(header.getOwnerId());
			place.putParam(ACTPlace.SUBMITTER_ID_FILTER_PARAM, header.getOwnerId());
			selectedUserBadge.configure(header.getOwnerId());
			peopleSuggestWidget.clear();
			view.setSelectedUserBadgeVisible(true);
			loadData();
		} else {
			onClearUserFilter();
		}
	}
	
	@Override
	public void onClearUserFilter() {
		submitterIdFilter = null;
		place.removeParam(ACTPlace.SUBMITTER_ID_FILTER_PARAM);
		view.setSelectedUserBadgeVisible(false);
		peopleSuggestWidget.clear();
		loadData();
	}
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
}
