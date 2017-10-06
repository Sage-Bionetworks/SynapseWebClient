package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.principal.TypeFilter;

import java.util.ArrayList;
import java.util.List;

public class InviteeSuggestionProvider {

	public static RegExp emailRegExp = RegExp.compile("[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}");

	private UserGroupSuggestionProvider userGroupSuggestionProvider;

	@Inject
	public InviteeSuggestionProvider(UserGroupSuggestionProvider userGroupSuggestionProvider) {
		this.userGroupSuggestionProvider = userGroupSuggestionProvider;
	}
	
	public void getSuggestions(TypeFilter type, final int offset, final int pageSize, final int width, final String prefix, final AsyncCallback<SynapseSuggestionBundle> callback) {
		if (emailRegExp.test(prefix)) {
			List<Suggestion> oneSuggestion = new ArrayList<>();
			oneSuggestion.add(new NewUserEmailSuggestion(prefix, width));
			SynapseSuggestionBundle suggestionBundle = new SynapseSuggestionBundle(oneSuggestion, 1);
			callback.onSuccess(suggestionBundle);
			return;
		}
		this.userGroupSuggestionProvider.getSuggestions(type, offset, pageSize, width, prefix, callback);
	}
}
