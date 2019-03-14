package org.sagebionetworks.web.client.widget.search;

import java.util.ArrayList;
import java.util.Collections;

import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;

public class EntitySearchSuggestOracle extends SuggestOracle {
	public static final int DELAY = 500;	// milliseconds
	public static final Long LIMIT = 30L;
	public SuggestOracle.Request request;
	public SuggestOracle.Callback callback;
	public int offset;
	public boolean isLoading = false;
	private GWTTimer timer;
	private SynapseJavascriptClient jsClient;
	private SynapseJSNIUtils jsniUtils;
	String searchTerm;
	ArrayList<EntitySuggestion> suggestions;
	@Inject
	public EntitySearchSuggestOracle(
			GWTTimer timer,
			SynapseJavascriptClient jsClient,
			SynapseJSNIUtils jsniUtils) {
		this.jsClient = jsClient;
		this.timer = timer;
		this.jsniUtils = jsniUtils;
		timer.configure(() -> {
			getSuggestions(offset);
		});
	}

	public SuggestOracle.Request getRequest()	{return request;}
	public SuggestOracle.Callback getCallback()	{return callback;}

	public void getSuggestions(final int offset) {
		if (!isLoading && !request.getQuery().equals(searchTerm) && !request.getQuery().isEmpty()) {
			DisplayUtils.scrollToTop();
			isLoading = true;
			searchTerm = request.getQuery();
			suggestions = new ArrayList<>();
			SearchQuery query = new SearchQuery();
			query.setQueryTerm(Collections.singletonList(searchTerm));
			query.setSize(LIMIT);
			query.setStart(new Long(offset));
			
			jsClient.getSearchResults(query, new AsyncCallback<SearchResults>() {
				@Override
				public void onSuccess(SearchResults result) {
					for (Hit hit : result.getHits()) {
						addSuggestion(hit.getName(), hit.getId());
					}
					onSuggestionsReady();
				}
				
				@Override
				public void onFailure(Throwable caught) {
					jsniUtils.consoleError(caught);
				}
			});
		}
	}

	public class EntitySuggestion implements SuggestOracle.Suggestion {
		String displayString;
		String entityId;
		public EntitySuggestion(String name, String entityId) {
			this.displayString = name + " (" + entityId + ")";
			this.entityId = entityId;
		}
		@Override
		public String getDisplayString() {
			return displayString;
		}
		@Override
		public String getReplacementString() {
			return displayString;
		}
		public String getEntityId() {
			return entityId;
		}
	}
	public void addSuggestion(String name, String entityId) {
		suggestions.add(new EntitySuggestion(name, entityId));
	}

	public void onSuggestionsReady() {
		SuggestOracle.Response response = new SuggestOracle.Response();
		response.setMoreSuggestions(false);
		response.setMoreSuggestionsCount(0);
		response.setSuggestions(suggestions);
		callback.onSuggestionsReady(request, response);
		isLoading = false;
	}

	@Override
	public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
		this.request = request;
		this.callback = callback;
		timer.cancel();
		timer.schedule(DELAY);
	}
}