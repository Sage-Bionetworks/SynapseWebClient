package org.sagebionetworks.web.client.widget.search;

import java.util.LinkedList;
import java.util.List;

import org.sagebionetworks.repo.model.UserGroupHeader;
import org.sagebionetworks.repo.model.UserGroupHeaderResponsePage;
import org.sagebionetworks.web.client.DisplayUtils;
import org.sagebionetworks.web.client.SynapseClientAsync;

import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;

public class UserGroupSuggestOracleImpl extends UserGroupSuggestOracle {
	private SuggestOracle.Request request;
	private SuggestOracle.Callback callback;
	private SynapseClientAsync synapseClient;
	boolean isLoading;
	public UserGroupSuggestBox suggestBox;
	private int pageSize;
	private int offset;
	private String searchText;
	private Timer timer = new Timer() {
		@Override
		public void run() {
			// If you backspace quickly the contents of the field are emptied but a
			// query for a single character is still executed. Workaround for this
			// is to check for an empty string field here.
			if (!request.getQuery().trim().isEmpty()) {
				suggestBox.setOffset(0);
				getSuggestions(offset, searchText);
			}
		}
		
	};
	
	@Inject
	public UserGroupSuggestOracleImpl(SynapseClientAsync synapseClient) {
		this.synapseClient = synapseClient;
	}
	
	@Override
	public void configure(final UserGroupSuggestBox suggestBox, int pageSize) {
		this.isLoading = false;
		this.suggestBox = suggestBox;
		this.pageSize = pageSize;
	}

	
	@Override
	public void getSuggestions(final int offset, final String searchText) {
		this.offset = offset;
		this.searchText = searchText;
		if (!isLoading) {
			isLoading = true;
			suggestBox.showLoading();
			String prefix = request.getQuery();
			final List<Suggestion> suggestions = new LinkedList<Suggestion>();
			synapseClient.getUserGroupHeadersByPrefix(prefix, pageSize, offset, new AsyncCallback<UserGroupHeaderResponsePage>() {
				@Override
				public void onSuccess(UserGroupHeaderResponsePage result) {
					// Update view fields.
					if (suggestBox != null)
						suggestBox.updateFieldStateForSuggestions(result, offset);
					
					// Load suggestions.
					for (UserGroupHeader header : result.getChildren()) {
						suggestions.add(makeUserGroupSuggestion(header, searchText));
					}
	
					// Set up response
					SuggestOracle.Response response = new SuggestOracle.Response(suggestions);
					callback.onSuggestionsReady(request, response);
					suggestBox.hideLoading();
					isLoading = false;
				}
				
				@Override
				public void onFailure(Throwable caught) {
//					if (!DisplayUtils.handleServiceException(caught, globalApplicationState,
//							authenticationController.isLoggedIn(), view)) {                    
//						presenter.showErrorMessage(caught.getMessage());
//					}
					suggestBox.handleOracleException(caught);
					suggestBox.hideLoading();
					isLoading = false;
				}
	
			});
		}
	}
	
	@Override
	public boolean isDisplayStringHTML() {
		return true;
	}
	
	@Override
	public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
		this.request = request;
		this.callback = callback;
		timer.cancel();
		timer.schedule(UserGroupSuggestBox.DELAY);
	}
	
	public SuggestOracle.Request getRequest()	{	return request;		}
	public SuggestOracle.Callback getCallback()	{	return callback;	}
	
	public UserGroupSuggestion makeUserGroupSuggestion(UserGroupHeader header, String prefix) {
		return new UserGroupSuggestion(header, prefix);
	}


	/*
	 * Suggestion
	 */
	public class UserGroupSuggestion implements IsSerializable, Suggestion {
		private UserGroupHeader header;
		private String prefix;
		
		public UserGroupSuggestion(UserGroupHeader header, String prefix) {
			this.header = header;
			this.prefix = prefix;
		}
		
		public UserGroupHeader getHeader()		{	return header;			}
		public String getPrefix() 				{	return prefix;			}
		public void setPrefix(String prefix)	{	this.prefix = prefix;	}
		
		@Override
		public String getDisplayString() {
			return DisplayUtils.getUserGroupDisplaySuggestionHtml(header, suggestBox.getWidth() + "px",
					suggestBox.getBaseFileHandleUrl(), suggestBox.getBaseProfileAttachmentUrl());
		}

		@Override
		public String getReplacementString() {
			// Example output:
			// Pac Man  |  114085
			StringBuilder sb = new StringBuilder();
			if (!header.getIsIndividual())
				sb.append("(Team) ");
			
			String firstName = header.getFirstName();
			String lastName = header.getLastName();
			String username = header.getUserName();
			sb.append(DisplayUtils.getDisplayName(firstName, lastName, username));
			sb.append("  |  " + header.getOwnerId());
			return sb.toString();
		}
		
	} // end inner class UserGroupSuggestion	
}
