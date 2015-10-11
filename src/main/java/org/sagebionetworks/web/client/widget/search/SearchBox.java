package org.sagebionetworks.web.client.widget.search;

import java.util.Arrays;

import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.schema.adapter.AdapterFactory;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.web.client.GlobalApplicationState;
import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.place.PeopleSearch;
import org.sagebionetworks.web.client.presenter.SearchUtil;
import org.sagebionetworks.web.client.security.AuthenticationController;
import org.sagebionetworks.web.client.widget.SynapseWidgetPresenter;
import org.sagebionetworks.web.shared.SearchQueryUtils;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SearchBox implements SearchBoxView.Presenter, SynapseWidgetPresenter {
	
	private SearchBoxView view;
	private GlobalApplicationState globalApplicationState;
	private AdapterFactory adapterFactory;
	private SynapseClientAsync synapseClient;
	private boolean searchAll = false;
	
	@Inject
	public SearchBox(SearchBoxView view, 
			AuthenticationController authenticationController,
			GlobalApplicationState globalApplicationState,
			AdapterFactory adapterFactory,
			SynapseClientAsync synapseClient) {
		this.view = view;
		this.globalApplicationState = globalApplicationState;
		this.adapterFactory = adapterFactory;
		this.synapseClient = synapseClient;
		view.setPresenter(this);
	}	
	
	@Override
	public Widget asWidget() {
		view.setPresenter(this);
		return view.asWidget();		
	}

	public void clearState() {
		view.clear();
	}

	@Override
	public void search(String value) {
		if (value.charAt(0) == '@') {
			globalApplicationState.getPlaceChanger().goTo(new PeopleSearch(value.substring(1)));
		} else {
			if (searchAll) {
				SearchQuery query = SearchQueryUtils.getAllTypesSearchQuery();
				query.setQueryTerm(Arrays.asList(value.split(" ")));
				try {
					value = query.writeToJSONObject(adapterFactory.createNew()).toJSONString();
				} catch (JSONObjectAdapterException e) {
					// if fail, fall back on regular search
				}
			}
			SearchUtil.searchForTerm(value, globalApplicationState, synapseClient);
		}
	}


	@Override
	public void setSearchAll(boolean searchAll) {
		this.searchAll = searchAll;
	}

	public void setVisible(boolean isVisible) {
		view.setVisible(isVisible);
	}

	
	/*
	 * Private Methods
	 */
}
