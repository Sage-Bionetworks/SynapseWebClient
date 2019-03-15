package org.sagebionetworks.web.client.widget.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sagebionetworks.repo.model.EntityHeader;
import org.sagebionetworks.repo.model.search.Hit;
import org.sagebionetworks.repo.model.search.SearchResults;
import org.sagebionetworks.repo.model.search.query.SearchQuery;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.StringUtils;
import org.sagebionetworks.web.client.SynapseJSNIUtils;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;

public class EntitySearchSuggestOracle extends SuggestOracle {
	public static final int DELAY = 500;	// milliseconds
	public static final Long LIMIT = 15L;
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

	public void getSuggestions(int offset) {
		if (!isLoading && !request.getQuery().isEmpty()) {
			DisplayUtils.scrollToTop();
			if (!request.getQuery().equals(searchTerm)) {
				getNewSuggestions(offset);
			} else {
				onSuggestionsReady();
			}
		}
	}
	
	public void getNewSuggestions(int offset) {
		isLoading = true;
		searchTerm = request.getQuery();
		suggestions = new ArrayList<>();
		SearchQuery query = new SearchQuery();
		query.setQueryTerm(Collections.singletonList(searchTerm));
		query.setSize(LIMIT);
		List<String> returnFields = new ArrayList<>();
		returnFields.add("path");
		query.setReturnFields(returnFields);
		query.setStart(new Long(offset));
		
		jsClient.getSearchResults(query, new AsyncCallback<SearchResults>() {
			@Override
			public void onSuccess(SearchResults result) {
				for (Hit hit : result.getHits()) {
					addSuggestion(hit);
				}
				onSuggestionsReady();
			}
			
			@Override
			public void onFailure(Throwable caught) {
				jsniUtils.consoleError(caught);
			}
		});
	}

	public class EntitySuggestion implements SuggestOracle.Suggestion {
		public static final String PATH_SEPARATOR = " / ";
		public static final int MAX_ENTITY_PATH_DISPLAY_LENGTH = 25;
		Hit hit;
		String displayString = "";
		public EntitySuggestion(Hit hit) {
			this.hit = hit;
			List<EntityHeader> entityPath = hit.getPath().getPath();
			entityPath.remove(0);
			EntityHeader hitEntity = entityPath.remove(entityPath.size()-1);
			for (EntityHeader header : entityPath) {
				displayString += PATH_SEPARATOR + header.getName();
			}
			displayString = StringUtils.truncateValues(displayString.trim(), MAX_ENTITY_PATH_DISPLAY_LENGTH, true);
			displayString += PATH_SEPARATOR + hitEntity.getName();
		}
		@Override
		public String getDisplayString() {
			return displayString;
		}
		@Override
		public String getReplacementString() {
			return hit.getName();
		}
		public String getEntityId() {
			return hit.getId();
		}
	}
	public void addSuggestion(Hit hit) {
		suggestions.add(new EntitySuggestion(hit));
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

	@Override
	public boolean isDisplayStringHTML() {
		return true;
	}
}