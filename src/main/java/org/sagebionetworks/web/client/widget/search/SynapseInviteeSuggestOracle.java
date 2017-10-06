package org.sagebionetworks.web.client.widget.search;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.inject.Inject;
import org.sagebionetworks.repo.model.principal.TypeFilter;
import org.sagebionetworks.web.client.GWTTimer;
import org.sagebionetworks.web.client.SynapseClientAsync;

public class SynapseInviteeSuggestOracle extends SuggestOracle {

	public Request request;
	public Callback callback;
	public SynapseClientAsync synapseClient;
	public int pageSize;
	public int offset;
	public boolean isLoading;
	public SynapseInviteeSuggestBox suggestBox;
	public InviteeSuggestionProvider provider;
	public String searchTerm;
	public String width;
	private GWTTimer timer;
	private TypeFilter type = TypeFilter.ALL;
	@Inject
	public SynapseInviteeSuggestOracle(GWTTimer timer) {
		this.timer = timer;
	}
	public void setTypeFilter(TypeFilter type) {
		this.type = type;
	}
	public void configure(final SynapseInviteeSuggestBox suggestBox,
			int pageSize,
			InviteeSuggestionProvider provider) {
		this.isLoading = false;
		this.suggestBox = suggestBox;
		this.pageSize = pageSize;
		this.provider = provider;
		this.timer.configure(new Runnable() {
			@Override
			public void run() {
				// If you backspace quickly the contents of the field are emptied but a
				// query for a single character is still executed. Workaround for this
				// is to check for an empty string field here.
				if (!suggestBox.getText().trim().isEmpty()) {
					offset = 0;
					suggestBox.setOffset(offset);
					getSuggestions(offset);
				}
			}
		});
	}
 	
	public Request getRequest()	{	return request;		}
	public Callback getCallback()	{	return callback;	}

	public void getSuggestions(final int offset) {
		if (!isLoading) {
			suggestBox.showLoading();
			//seachTerm or request.getQuery?
			provider.getSuggestions(type, offset, pageSize, suggestBox.getWidth(), request.getQuery(), new AsyncCallback<SynapseSuggestionBundle>() {
				@Override
				public void onSuccess(SynapseSuggestionBundle suggestionBundle) {
					suggestBox.hideLoading();
					if (suggestBox != null) {
						suggestBox.updateFieldStateForSuggestions((int)suggestionBundle.getTotalNumberOfResults(), offset);
					}
					Response response = new Response(suggestionBundle.getSuggestionBundle());
					callback.onSuggestionsReady(request, response);
					isLoading = false;
				}

				@Override
				public void onFailure(Throwable caught) {
					suggestBox.hideLoading();
					suggestBox.handleOracleException(caught);
				}	
			});
		}
	}
	
	@Override
	public void requestSuggestions(Request request, Callback callback) {
		this.request = request;
		this.callback = callback;
		timer.cancel();
		timer.schedule(SynapseUserGroupSuggestBox.DELAY);
	}	
	
	@Override
	public boolean isDisplayStringHTML() {
		return true;
	}
	
	public void setWidth(String width) {
		this.width = width;
	}
	
}