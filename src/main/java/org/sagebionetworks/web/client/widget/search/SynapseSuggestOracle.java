package org.sagebionetworks.web.client.widget.search;

import java.util.List;

import org.sagebionetworks.web.client.SynapseClientAsync;
import org.sagebionetworks.web.client.utils.CallbackP;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.SuggestOracle;

public class SynapseSuggestOracle extends SuggestOracle {

	public SuggestOracle.Request request;
	public SuggestOracle.Callback callback;
	public SynapseClientAsync synapseClient;
	public int pageSize;
	public int offset;
	public boolean isLoading;
	public UserGroupSuggestBox suggestBox;
	public SuggestionProvider provider;
	public String searchTerm;

	public void configure(final UserGroupSuggestBox suggestBox, int pageSize, SuggestionProvider provider) {
		this.isLoading = false;
		this.suggestBox = suggestBox;
		this.pageSize = pageSize;
		this.provider = provider;
		this.provider.configure(String.valueOf(suggestBox.getWidth()), suggestBox.getBaseFileHandleUrl(),
				suggestBox.getBaseProfileAttachmentUrl());
	}
	
	private Timer timer = new Timer() {
		@Override
		public void run() {
			// If you backspace quickly the contents of the field are emptied but a
			// query for a single character is still executed. Workaround for this
			// is to check for an empty string field here.
			if (!request.getQuery().trim().isEmpty()) {
				offset = 0;
				suggestBox.setOffset(offset);
				getSuggestions(offset, searchTerm);
			}
		}
		
	};
	
	public SuggestOracle.Request getRequest()	{	return request;		}
	public SuggestOracle.Callback getCallback()	{	return callback;	}

	public void getSuggestions(final int offset, final String searchTerm) {
		if (!isLoading) {
			suggestBox.showLoading();
			try {
				provider.getSuggestions(offset, pageSize, request.getQuery(), new CallbackP<List<SynapseSuggestion>>() {
					@Override
					public void invoke(List<SynapseSuggestion> suggestions) {
						suggestBox.hideLoading();
						// Update view fields.
						if (suggestBox != null) {
							suggestBox.updateFieldStateForSuggestions(suggestions.size(), offset);
						}
						// Load suggestions.
//						
//						for (UserGroupHeader header : result.getChildren()) {
//							suggestions.add(makeUserGroupSuggestion(header, searchTerm));
//						}
						// Set up response
						SuggestOracle.Response response = new SuggestOracle.Response(suggestions);
						callback.onSuggestionsReady(request, response);
						suggestBox.hideLoading();
						isLoading = false;
					}	
				});
			} catch (Throwable caught) {
				suggestBox.handleOracleException(caught);
				suggestBox.hideLoading();
				isLoading = false;
			}
		}
		
	}
	
	@Override
	public void requestSuggestions(SuggestOracle.Request request, SuggestOracle.Callback callback) {
		this.request = request;
		this.callback = callback;
		timer.cancel();
		timer.schedule(UserGroupSuggestBox.DELAY);
	}	
	
	@Override
	public boolean isDisplayStringHTML() {
		return true;
	}
	
}