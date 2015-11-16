package org.sagebionetworks.web.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.verification.VerificationPagedResults;
import org.sagebionetworks.repo.model.verification.VerificationStateEnum;
import org.sagebionetworks.repo.model.verification.VerificationSubmission;
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
	private ACTPlace place;
	private ACTView view;
	private UserProfileClientAsync userProfileClient;
	private PortalGinInjector ginInjector;
	private SynapseAlert synAlert;
	SynapseSuggestBox peopleSuggestWidget;
	private VerificationStateEnum stateFilter;
	private Long submitterIdFilter;
	
	public static Long LIMIT = 100L;
	
	@Inject
	public ACTPresenter(ACTView view,
			UserProfileClientAsync userProfileClient,
			SynapseAlert synAlert,
			SynapseSuggestBox peopleSuggestBox,
			UserGroupSuggestionProvider provider,
			PortalGinInjector ginInjector) {
		this.view = view;
		this.userProfileClient = userProfileClient;
		this.synAlert = synAlert;
		this.peopleSuggestWidget = peopleSuggestBox;
		this.ginInjector = ginInjector;
		peopleSuggestWidget.setSuggestionProvider(provider);
		
		view.setPresenter(this);
		view.setSynAlert(synAlert.asWidget());
		List<String> states = new ArrayList<String>();
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
		//initial load
		submitterIdFilter = null;
		stateFilter = null;
		loadData();
	}
	
	public void loadData() {
		loadData(0L);
	}
	
	@Override
	public void loadData(final Long offset) {
		view.clearRows();
		synAlert.clear();
		userProfileClient.listVerificationSubmissions(stateFilter, submitterIdFilter, LIMIT, offset, new AsyncCallback<VerificationPagedResults>() {
			@Override
			public void onSuccess(VerificationPagedResults results) {
				
				for (VerificationSubmission submission : results.getResults()) {
					VerificationSubmissionWidget w = ginInjector.getVerificationSubmissionWidget();
					w.configure(submission, true, false);
					view.addRow(w.asWidget());
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
		final String token = place.toToken();
	}

	@Override
	public void onApplyStateFilter() {
		String selectedState = view.getSelectedState();
		if (NO_STATE_FILTER.equals(selectedState)) {
			stateFilter = null;
		} else {
			stateFilter = VerificationStateEnum.valueOf(selectedState);	
		}
		loadData();
	}
	
	@Override
	public void onApplyUserFilter() {
		UserGroupSuggestion suggestion = (UserGroupSuggestion)peopleSuggestWidget.getSelectedSuggestion();
		if(suggestion != null) {
			UserGroupHeader header = suggestion.getHeader();
			submitterIdFilter = Long.parseLong(header.getOwnerId());
		} else {
			submitterIdFilter = null;
		}
		loadData();
	}
	
	@Override
    public String mayStop() {
        view.clear();
        return null;
    }
	
	
	
}
