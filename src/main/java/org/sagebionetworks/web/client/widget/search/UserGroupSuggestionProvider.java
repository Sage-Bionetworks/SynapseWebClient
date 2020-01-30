package org.sagebionetworks.web.client.widget.search;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.SynapseJavascriptClient;
import org.sagebionetworks.web.client.SynapseProperties;
import org.sagebionetworks.web.shared.PublicPrincipalIds;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class UserGroupSuggestionProvider {
	protected SynapseJavascriptClient jsClient;
	private HashSet<String> publicPrincipalIds = new HashSet<String>();

	@Inject
	public UserGroupSuggestionProvider(SynapseJavascriptClient jsClient, SynapseProperties synapseProperties) {
		this.jsClient = jsClient;
		PublicPrincipalIds ids = synapseProperties.getPublicPrincipalIds();
		publicPrincipalIds.add(ids.getAnonymousUserPrincipalId().toString());
		publicPrincipalIds.add(ids.getAuthenticatedAclPrincipalId().toString());
		publicPrincipalIds.add(ids.getPublicAclPrincipalId().toString());
	}

	public void getSuggestions(TypeFilter type, final int offset, final int pageSize, final int width, final String prefix, final AsyncCallback<SynapseSuggestionBundle> callback) {
		jsClient.getUserGroupHeadersByPrefix(prefix, type, pageSize, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
			@Override
			public void onSuccess(UserGroupHeaderResponsePage result) {
				List<UserGroupSuggestion> suggestions = new LinkedList<>();
				for (UserGroupHeader header : result.getChildren()) {
					if (!publicPrincipalIds.contains(header.getOwnerId())) {
						suggestions.add(new UserGroupSuggestion(header, prefix, width));
					}
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
