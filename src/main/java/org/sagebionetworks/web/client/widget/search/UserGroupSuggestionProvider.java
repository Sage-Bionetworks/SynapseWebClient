package org.sagebionetworks.web.client.widget.search;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;

public class UserGroupSuggestionProvider {
	protected SynapseJavascriptClient jsClient;

	@Inject
	public UserGroupSuggestionProvider(SynapseJavascriptClient jsClient) {
		this.jsClient = jsClient;
	}

	public void getSuggestions(TypeFilter type, final int offset, final int pageSize, final int width, final String prefix, final AsyncCallback<SynapseSuggestionBundle> callback) {
		jsClient.getUserGroupHeadersByPrefix(prefix, type, pageSize, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				List<Suggestion> suggestions = new LinkedList<Suggestion>();
				for (UserGroupHeader header : result.getChildren()) {
					suggestions.add(new UserGroupSuggestion(header, prefix, width));
				}
				SynapseSuggestionBundle suggestionBundle = new SynapseSuggestionBundle(suggestions, result.getTotalNumberOfResults());
				callback.onSuccess(suggestionBundle);
			}

			@Override
			public void onFailure(Throwable caught) {
				callback.onFailure(caught);
			}
		});
	}
}
