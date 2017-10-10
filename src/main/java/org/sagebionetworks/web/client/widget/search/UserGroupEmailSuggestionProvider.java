package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.SynapseJavascriptClient;

import java.util.LinkedList;
import java.util.List;

public class UserGroupEmailSuggestionProvider extends UserGroupSuggestionProvider {
	private static RegExp emailRegExp = RegExp.compile("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}");

	@Inject
	public UserGroupEmailSuggestionProvider(SynapseJavascriptClient jsClient) {
		super(jsClient);
	}

	public void getSuggestions(TypeFilter type, final int offset, final int pageSize, final int width, final String prefix, final AsyncCallback<SynapseSuggestionBundle> callback) {
		if (emailRegExp.test(prefix)) {
			UserGroupHeader header = new UserGroupHeader();
			header.setEmail(prefix);
			List<Suggestion> suggestions = new LinkedList<Suggestion>();
			suggestions.add(new UserGroupEmailSuggestion(prefix, width));
			SynapseSuggestionBundle suggestionBundle = new SynapseSuggestionBundle(suggestions, 1);
			callback.onSuccess(suggestionBundle);
		} else {
			jsClient.getUserGroupHeadersByPrefix(prefix, type, pageSize, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
				@Override
				public void onSuccess(UserGroupHeaderResponsePage result) {
					List<Suggestion> suggestions = new LinkedList<Suggestion>();
					for (UserGroupHeader header : result.getChildren()) {
						suggestions.add(new UserGroupEmailSuggestion(header, prefix, width));
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
}
