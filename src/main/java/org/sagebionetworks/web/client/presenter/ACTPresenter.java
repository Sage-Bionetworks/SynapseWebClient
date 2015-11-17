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
import org.sagebionetworks.web.client.view.ACTView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;
import org.sagebionetworks.web.client.widget.search.SynapseSuggestBox;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider;
import org.sagebionetworks.web.client.widget.search.UserGroupSuggestionProvider.UserGroupSuggestion;
import org.sagebionetworks.web.client.widget.verification.VerificationSubmissionWidget;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class ACTPresenter extends AbstractActivity implements ACTView.Presenter, Presenter<ACTPlace> {
	private static final String NO_STATE_FILTER = "-No filter-";
	private static final String SUBMITTER_ID_FILTER_PARAM = "submitterID";
	private static final String STATE_FILTER_PARAM = "state";
	private ACTPlace place;
	private ACTView view;
	private UserProfileClientAsync userProfileClient;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	private GlobalApplicationState globalAppState;
	SynapseSuggestBox peopleSuggestWidget;
	private VerificationStateEnum stateFilter;
	private Long submitterIdFilter;
	
	public static Long LIMIT = 100L;
	List<String> states;
	
	@Inject
	public ACTPresenter(ACTView view,
			UserProfileClientAsync userProfileClient,
			SynapseAlert synAlert,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider,
			PortalGinInjector ginInjector,
			GlobalApplicationState globalAppState) {
		this.view = view;
		this.userProfileClient = userProfileClient;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		this.ginInjector = ginInjector;
		this.globalAppState = globalAppState;
		peopleSuggestWidget.setSuggestionProvider(provider);
		
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		states = new ArrayList<String>();
		states.add(NO_STATE_FILTER);
		for (VerificationStateEnum state : VerificationStateEnum.values()) {
			states.add(state.toString());
		}
		view.setStates(states);
		view.setUserPickerWidget(peopleSuggestWidget.asWidget());
	}

	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
		peopleSuggestWidget.setPlaceholderText("Enter a user name...");
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
		String stateFilterParam = place.getParam(STATE_FILTER_PARAM);
		if (stateFilterParam != null) {
			stateFilter = VerificationStateEnum.valueOf(stateFilterParam);
			view.setSelectedState(states.indexOf(stateFilterParam));
		}
		String submitterIdFilterParam =  place.getParam(SUBMITTER_ID_FILTER_PARAM);
		if (submitterIdFilterParam != null) {
			submitterIdFilter = Long.parseLong(submitterIdFilterParam);
			peopleSuggestWidget.setText(submitterIdFilterParam);
		}
	}

	@Override
	public void onApplyStateFilter() {
		String selectedState = view.getSelectedState();
		if (NO_STATE_FILTER.equals(selectedState)) {
			stateFilter = null;
			place.removeParam(STATE_FILTER_PARAM);
		} else {
			stateFilter = VerificationStateEnum.valueOf(selectedState);
			place.putParam(STATE_FILTER_PARAM, selectedState);	
		}
		loadData();
	}
	
	@Override
	public void onApplyUserFilter() {
		UserGroupSuggestion suggestion = (UserGroupSuggestion)peopleSuggestWidget.getSelectedSuggestion();
		if(suggestion != null) {
			UserGroupHeader header = suggestion.getHeader();
			submitterIdFilter = Long.parseLong(header.getOwnerId());
			place.putParam(SUBMITTER_ID_FILTER_PARAM, header.getOwnerId());
		} else {
			submitterIdFilter = null;
			place.removeParam(SUBMITTER_ID_FILTER_PARAM);
		}
		loadData();
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
	
	
}
