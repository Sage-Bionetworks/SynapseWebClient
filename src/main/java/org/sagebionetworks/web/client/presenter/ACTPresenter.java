package org.sagebionetworks.web.client.presenter;

import static org.sagebionetworks.web.client.ServiceEntryPointUtils.fixServiceEntryPoint;
import java.util.ArrayList;
import java.util.List;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.PortalGinInjector;
import org.sagebionetworks.web.client.UserProfileClientAsync;
import org.sagebionetworks.web.client.place.ACTPlace;
import org.sagebionetworks.web.client.utils.Callback;
import org.sagebionetworks.web.client.utils.CallbackP;
import org.sagebionetworks.web.client.view.ACTView;
import org.sagebionetworks.web.client.widget.LoadMoreWidgetContainer;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
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
	public static Long LIMIT = 30L;
	List<String> states;
	LoadMoreWidgetContainer loadMoreContainer;
	Long currentOffset;

	@Inject
	public ACTPresenter(ACTView view, UserProfileClientAsync userProfileClient, SynapseAlert synAlert, SynapseSuggestBox peopleSuggestBox, UserGroupSuggestionProvider provider, PortalGinInjector ginInjector, GlobalApplicationState globalAppState, UserBadge selectedUserBadge, LoadMoreWidgetContainer loadMoreContainer) {
		this.view = view;
		this.userProfileClient = userProfileClient;
		fixServiceEntryPoint(userProfileClient);
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		this.ginInjector = ginInjector;
		this.globalAppState = globalAppState;
		this.selectedUserBadge = selectedUserBadge;
		this.loadMoreContainer = loadMoreContainer;
		peopleSuggestWidget.setSuggestionProvider(provider);
		peopleSuggestWidget.setTypeFilter(TypeFilter.USERS_ONLY);
		view.setPresenter(this);
		view.setLoadMoreContainer(loadMoreContainer.asWidget());
		view.setSynAlert(synAlert.asWidget());
		states = new ArrayList<String>();
		for (VerificationStateEnum state : VerificationStateEnum.values()) {
			states.add(state.toString());
		}
		view.setStates(states);
		view.setUserPickerWidget(peopleSuggestWidget.asWidget());
		view.setSelectedUserBadge(selectedUserBadge.asWidget());
		view.setSelectedStateText("");
		peopleSuggestBox.addItemSelectedHandler(new CallbackP<UserGroupSuggestion>() {
			@Override
			public void invoke(UserGroupSuggestion suggestion) {
				onUserSelected(suggestion);
			}
		});
		loadMoreContainer.configure(new Callback() {
			@Override
			public void invoke() {
				loadMore();
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
		loadMoreContainer.clear();
		currentOffset = 0L;
		loadMore();
	}

	public void loadMore() {
		synAlert.clear();
		globalAppState.pushCurrentPlace(place);
		userProfileClient.listVerificationSubmissions(stateFilter, submitterIdFilter, LIMIT, currentOffset, new AsyncCallback<VerificationPagedResults>() {
			@Override
			public void onSuccess(VerificationPagedResults results) {
				currentOffset += LIMIT;
				loadMoreContainer.setIsMore(!results.getResults().isEmpty());
				boolean isACT = true;
				boolean isModal = false;
				for (VerificationSubmission submission : results.getResults()) {
					VerificationSubmissionWidget w = ginInjector.getVerificationSubmissionWidget();
					w.configure(submission, isACT, isModal);
					loadMoreContainer.add(w.asWidget());
					w.show();
				}
			}

			@Override
			public void onFailure(Throwable caught) {
				loadMoreContainer.setIsMore(false);
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
			stateFilter = VerificationStateEnum.valueOf(stateFilterParam.toUpperCase());
			view.setSelectedStateText(stateFilterParam);
		}
		String submitterIdFilterParam = place.getParam(ACTPlace.SUBMITTER_ID_FILTER_PARAM);
		if (submitterIdFilterParam != null) {
			submitterIdFilter = Long.parseLong(submitterIdFilterParam);
			selectedUserBadge.configure(submitterIdFilterParam);
			view.setSelectedUserBadgeVisible(true);
		}
	}

	/**
	 * For testing
	 * 
	 * @return
	 */
	public ACTPlace getPlace() {
		return place;
	}

	@Override
	public void onStateSelected(String selectedState) {
		stateFilter = VerificationStateEnum.valueOf(selectedState.toUpperCase());
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
		if (suggestion != null) {
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
