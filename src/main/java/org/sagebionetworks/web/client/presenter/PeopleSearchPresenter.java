package org.sagebionetworks.web.client.presenter;

import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.view.PeopleSearchView;
import org.sagebionetworks.web.client.widget.entity.controller.SynapseAlert;
import org.sagebionetworks.web.client.widget.search.PaginationEntry;
import org.sagebionetworks.web.client.widget.search.PaginationUtil;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

public class PeopleSearchPresenter extends AbstractActivity implements PeopleSearchView.Presenter, Presenter<PeopleSearch> {
	
	public static final int SEARCH_PEOPLE_LIMIT = 10;
	
	private PeopleSearch place;
	private PeopleSearchView view;
	private AuthenticationController authenticationController;
	private GlobalApplicationState globalApplicationState;
	private SynapseClientAsync synapseClient;
	private SynapseAlert synAlert;
	
	private int offset;
	private String searchTerm;
	private UserGroupHeaderResponsePage peopleList;
	
	@Inject
	public PeopleSearchPresenter(PeopleSearchView view,
			SynapseClientAsync synapseClient,
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			SynapseAlert synAlert) {
		this.view = view;
		this.authenticationController = authenticationController;
		this.globalApplicationState = globalApplicationState;
		this.synapseClient = synapseClient;
		this.synAlert = synAlert;
		view.setPresenter(this);
	}
	
	@Override
	public void start(AcceptsOneWidget panel, EventBus eventBus) {
		// Install the view
		panel.setWidget(view);
	}

	@Override
	public void setPlace(PeopleSearch place) {
		this.place = place;
		this.view.setPresenter(this);
		this.view.clear();
		view.setSynAlertWidget(synAlert.asWidget());
		showView(place);
	}
	
	@Override
	public void goTo(Place place) {
		globalApplicationState.getPlaceChanger().goTo(place);
	}
	
	@Override
	public void search(final String searchTerm, Integer offset) {
		this.synAlert.clear();
		this.searchTerm = searchTerm;
		if (offset == null)
			this.offset = 0;
		else
			this.offset = offset;
		//execute search, and update view with the results
		AsyncCallback<UserGroupHeaderResponsePage> callback = 
				new AsyncCallback<UserGroupHeaderResponsePage>() {

			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				peopleList = result;
				view.configure(peopleList.getChildren(), searchTerm);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				view.clear();
				view.setSynAlertWidgetVisible(true);
				synAlert.handleException(caught);
			}
			
		};
		synapseClient.getUserGroupHeadersByPrefix(searchTerm, (long) SEARCH_PEOPLE_LIMIT, (long) this.offset, callback);
	}
	
	@Override
	public List<PaginationEntry> getPaginationEntries(int nPerPage,
			int nPagesToShow) {
		Long nResults = peopleList.getTotalNumberOfResults();
		if(nResults == null)
			return null;
		return PaginationUtil.getPagination(nResults.intValue(), offset, nPerPage, nPagesToShow);
	}
	
	@Override
	public int getOffset() {
		return offset;
	}
	
	
	/*
	 * Private Methods
	 */
	
	private void showView(PeopleSearch place) {
		String searchTerm = place.getSearchTerm();
		Integer offset = place.getStart();
		search(searchTerm, offset);
	}

}
